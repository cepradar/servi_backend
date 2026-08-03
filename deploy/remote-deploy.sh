#!/usr/bin/env bash

set -Eeuo pipefail

DEPLOY_PATH="${1:-${DEPLOY_PATH:-}}"
DEPLOY_BRANCH="${2:-${DEPLOY_BRANCH:-}}"
DEPLOY_COMMIT="${3:-${DEPLOY_COMMIT:-}}"
DEPLOY_SERVICE="${4:-${DEPLOY_SERVICE:-}}"
EXPECTED_REPOSITORY="${5:-${EXPECTED_REPOSITORY:-}}"
EXPECTED_JAR_GLOB="${6:-${EXPECTED_JAR_GLOB:-target/inventory-backend-*.jar}}"
HEALTHCHECK_URL="${7:-${HEALTHCHECK_URL:-}}"

phase="initialization"
previous_commit=""
backup_jar=""
migration_changed="false"
rollback_possible="false"

log() {
  printf '[deploy][%s] %s\n' "$phase" "$1"
}

fail() {
  printf '::error::[%s] %s\n' "$phase" "$1" >&2
  exit 1
}

require_value() {
  local name="$1"
  local value="$2"
  if [ -z "$value" ]; then
    fail "Falta el valor requerido: $name"
  fi
}

matches_expected_repository() {
  local remote_url="$1"
  case "$remote_url" in
    *"/${EXPECTED_REPOSITORY}.git"|*"/${EXPECTED_REPOSITORY}"|*":${EXPECTED_REPOSITORY}.git"|*":${EXPECTED_REPOSITORY}")
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

dump_service_logs() {
  sudo systemctl status "$DEPLOY_SERVICE" --no-pager || true
  sudo journalctl -u "$DEPLOY_SERVICE" -n 80 --no-pager || true
}

attempt_rollback() {
  if [ "$rollback_possible" != "true" ]; then
    log "Rollback automático no disponible."
    return
  fi

  if [ "$migration_changed" = "true" ]; then
    log "Rollback automático omitido: el despliegue incluye cambios en migraciones Flyway."
    return
  fi

  if [ ! -f "$backup_jar" ]; then
    log "Rollback automático omitido: no existe respaldo del JAR anterior."
    return
  fi

  log "Intentando rollback al commit anterior: $previous_commit"

  cp "$backup_jar" "$DEPLOY_PATH/app.jar"
  git -C "$DEPLOY_PATH" reset --hard "$previous_commit"
  sudo systemctl restart "$DEPLOY_SERVICE"
  sleep 10

  if sudo systemctl is-active --quiet "$DEPLOY_SERVICE"; then
    log "Rollback completado. El servicio volvió a iniciar."
  else
    log "El rollback no logró dejar el servicio activo."
    dump_service_logs
  fi
}

on_error() {
  local exit_code=$?
  trap - ERR
  set +e

  printf '::error::Deployment failed during phase: %s (exit code %s)\n' "$phase" "$exit_code" >&2

  if [ -n "${DEPLOY_SERVICE:-}" ]; then
    dump_service_logs
    attempt_rollback
  fi

  exit "$exit_code"
}

trap on_error ERR

require_value "DEPLOY_PATH" "$DEPLOY_PATH"
require_value "DEPLOY_BRANCH" "$DEPLOY_BRANCH"
require_value "DEPLOY_COMMIT" "$DEPLOY_COMMIT"
require_value "DEPLOY_SERVICE" "$DEPLOY_SERVICE"
require_value "EXPECTED_REPOSITORY" "$EXPECTED_REPOSITORY"

phase="preflight"
log "Validando prerrequisitos del servidor."

[ -d "$DEPLOY_PATH" ] || fail "La ruta de despliegue no existe: $DEPLOY_PATH"
[ -f "$DEPLOY_PATH/mvnw" ] || fail "No se encontró mvnw en $DEPLOY_PATH"
[ -f "$DEPLOY_PATH/pom.xml" ] || fail "No se encontró pom.xml en $DEPLOY_PATH"

command -v git >/dev/null 2>&1 || fail "git no está instalado"
command -v bash >/dev/null 2>&1 || fail "bash no está instalado"
command -v curl >/dev/null 2>&1 || fail "curl no está instalado"
command -v java >/dev/null 2>&1 || fail "java no está instalado"
command -v sudo >/dev/null 2>&1 || fail "sudo no está instalado"
command -v ss >/dev/null 2>&1 || fail "ss no está instalado"

git -C "$DEPLOY_PATH" rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "La ruta no es un repositorio git válido"

origin_url="$(git -C "$DEPLOY_PATH" config --get remote.origin.url)"
[ -n "$origin_url" ] || fail "No se encontró remote.origin.url"
matches_expected_repository "$origin_url" || fail "El repositorio remoto '$origin_url' no coincide con '$EXPECTED_REPOSITORY'"

tracked_changes="$(git -C "$DEPLOY_PATH" status --porcelain --untracked-files=no)"
[ -z "$tracked_changes" ] || fail "Hay cambios locales controlados por git en el servidor. El despliegue se detiene para no sobrescribirlos."

lock_dir="$DEPLOY_PATH/.git"
lock_file="$lock_dir/deploy.lock"
exec 9>"$lock_file"
if command -v flock >/dev/null 2>&1; then
  flock -n 9 || fail "Ya hay otro despliegue en progreso para este repositorio."
else
  log "flock no está disponible; se continuará sin bloqueo de concurrencia local."
fi

phase="fetch"
log "Obteniendo cambios del remoto."

git -C "$DEPLOY_PATH" fetch --prune origin
git -C "$DEPLOY_PATH" show-ref --verify --quiet "refs/remotes/origin/$DEPLOY_BRANCH" || fail "La rama remota origin/$DEPLOY_BRANCH no existe"
git -C "$DEPLOY_PATH" cat-file -e "$DEPLOY_COMMIT^{commit}" || fail "El commit $DEPLOY_COMMIT no existe en el repositorio local tras fetch"
git -C "$DEPLOY_PATH" merge-base --is-ancestor "$DEPLOY_COMMIT" "origin/$DEPLOY_BRANCH" || fail "El commit $DEPLOY_COMMIT no pertenece a origin/$DEPLOY_BRANCH"

previous_commit="$(git -C "$DEPLOY_PATH" rev-parse HEAD)"

if git -C "$DEPLOY_PATH" diff --name-only "$previous_commit" "$DEPLOY_COMMIT" | grep -Eq '^src/main/resources/db/migration/'; then
  migration_changed="true"
  log "Se detectaron cambios en migraciones Flyway. El rollback automático quedará deshabilitado."
fi

phase="checkout"
log "Alineando el árbol de trabajo exactamente al commit del workflow."

git -C "$DEPLOY_PATH" checkout "$DEPLOY_BRANCH"
git -C "$DEPLOY_PATH" reset --hard "$DEPLOY_COMMIT"

phase="build"
log "Compilando la aplicación en el servidor."

chmod +x "$DEPLOY_PATH/mvnw"
(cd "$DEPLOY_PATH" && ./mvnw --batch-mode -DskipTests clean package)

jar_name_pattern="${EXPECTED_JAR_GLOB##*/}"
jar_path="$(find "$DEPLOY_PATH/target" -maxdepth 1 -type f -name "$jar_name_pattern" ! -name '*.original' | head -n 1)"
[ -n "$jar_path" ] || fail "No se encontró el JAR generado en target/"
[ -f "$jar_path" ] || fail "La ruta del JAR generado no existe: $jar_path"

phase="artifact-install"
log "Instalando el artefacto compilado."

backup_dir="$DEPLOY_PATH/.deploy-backups"
mkdir -p "$backup_dir"

timestamp="$(date +%Y%m%d%H%M%S)"
backup_jar="$backup_dir/app-$timestamp-${previous_commit:0:7}.jar"

if [ -f "$DEPLOY_PATH/app.jar" ]; then
  cp "$DEPLOY_PATH/app.jar" "$backup_jar"
  rollback_possible="true"
fi

install -m 0644 "$jar_path" "$DEPLOY_PATH/app.jar.new"
mv "$DEPLOY_PATH/app.jar.new" "$DEPLOY_PATH/app.jar"

phase="service-restart"
log "Reiniciando el servicio systemd: $DEPLOY_SERVICE"

sudo systemctl restart "$DEPLOY_SERVICE"

for _ in $(seq 1 30); do
  if sudo systemctl is-active --quiet "$DEPLOY_SERVICE"; then
    break
  fi
  sleep 2
done

sudo systemctl is-active --quiet "$DEPLOY_SERVICE" || fail "El servicio no quedó activo después del reinicio"

phase="process-verification"
log "Verificando el proceso principal del servicio."

main_pid="$(sudo systemctl show -p MainPID --value "$DEPLOY_SERVICE")"
case "$main_pid" in
  ''|0|*[!0-9]*)
    fail "systemd no reportó un MainPID válido para $DEPLOY_SERVICE"
    ;;
esac

phase="runtime-config"
log "Cargando configuración de runtime para validaciones posteriores."

if [ -f "$DEPLOY_PATH/.env" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$DEPLOY_PATH/.env"
  set +a
fi

server_port="${SERVER_PORT:-8080}"

phase="port-check"
log "Verificando que el puerto $server_port esté escuchando."

for _ in $(seq 1 30); do
  if ss -ltn | awk 'NR > 1 {print $4}' | grep -Eq ":${server_port}$"; then
    break
  fi
  sleep 2
done

ss -ltn | awk 'NR > 1 {print $4}' | grep -Eq ":${server_port}$" || fail "La aplicación no está escuchando en el puerto $server_port"

phase="health-check"

if [ -z "$HEALTHCHECK_URL" ]; then
  HEALTHCHECK_URL="http://127.0.0.1:${server_port}/actuator/health/readiness"
fi

log "Consultando el endpoint de salud: $HEALTHCHECK_URL"

for _ in $(seq 1 30); do
  if curl --fail --silent --show-error "$HEALTHCHECK_URL"; then
    printf '\n'
    break
  fi
  sleep 2
done

curl --fail --silent --show-error "$HEALTHCHECK_URL" >/dev/null || fail "La aplicación no respondió correctamente en $HEALTHCHECK_URL"

phase="completed"
log "Despliegue completado correctamente en el commit $DEPLOY_COMMIT"
