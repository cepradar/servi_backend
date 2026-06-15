package com.inventory.service;

import com.inventory.dto.SedeDto;
import com.inventory.dto.SedeRegistroDto;
import com.inventory.dto.UsuarioSedeDto;
import com.inventory.model.Sede;
import com.inventory.model.UsuarioSede;
import com.inventory.model.User;
import com.inventory.repository.SedeRepository;
import com.inventory.repository.UsuarioSedeRepository;
import com.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class SedeService {

    private static final Logger log = LoggerFactory.getLogger(SedeService.class);

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private UsuarioSedeRepository usuarioSedeRepository;

    @Autowired
    private UserRepository userRepository;

    // ── CRUD Sedes ──────────────────────────────────────────────────────────

    /**
     * Crea una nueva sede. El código de sede es provisto por el cliente y debe ser único.
     */
    public SedeDto crearSede(SedeRegistroDto dto) {
        Objects.requireNonNull(dto, "dto no puede ser null");
        String codigo = Objects.requireNonNull(dto.getCodigoSede(), "codigoSede es obligatorio").trim().toUpperCase();

        if (sedeRepository.existsById(codigo)) {
            throw new IllegalArgumentException("Ya existe una sede con el código: " + codigo);
        }

        Sede sede = new Sede();
        sede.setCodigoSede(codigo);
        sede.setNombre(Objects.requireNonNull(dto.getNombre(), "nombre es obligatorio").trim());
        sede.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        sede.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        sede.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        sede.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        if (dto.getPrefijoVentas() != null) sede.setPrefijoCodigoVenta(dto.getPrefijoVentas().trim().toUpperCase());
        if (dto.getPrefijoOrdenes() != null) sede.setPrefijoCodigoOrden(dto.getPrefijoOrdenes().trim().toUpperCase());

        Sede guardada = sedeRepository.save(sede);
        log.info("Sede creada: {} - {}", guardada.getCodigoSede(), guardada.getNombre());
        return convertirADto(guardada);
    }

    /**
     * Actualiza los datos de una sede existente. El código y los consecutivos
     * no son modificables por este endpoint.
     */
    public SedeDto actualizarSede(String codigoSede, SedeRegistroDto dto) {
        Objects.requireNonNull(codigoSede, "codigoSede es obligatorio");
        Objects.requireNonNull(dto, "dto no puede ser null");

        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            sede.setNombre(dto.getNombre().trim());
        }
        if (dto.getDireccion() != null) sede.setDireccion(dto.getDireccion().trim());
        if (dto.getTelefono() != null) sede.setTelefono(dto.getTelefono().trim());
        if (dto.getEmail() != null) sede.setEmail(dto.getEmail().trim());
        if (dto.getActivo() != null) sede.setActivo(dto.getActivo());
        if (dto.getPrefijoVentas() != null) sede.setPrefijoCodigoVenta(dto.getPrefijoVentas().trim().toUpperCase());
        if (dto.getPrefijoOrdenes() != null) sede.setPrefijoCodigoOrden(dto.getPrefijoOrdenes().trim().toUpperCase());

        Sede actualizada = sedeRepository.save(sede);
        log.info("Sede actualizada: {}", actualizada.getCodigoSede());
        return convertirADto(actualizada);
    }

    /**
     * Activa o desactiva una sede. No elimina los datos.
     */
    public SedeDto cambiarEstadoSede(String codigoSede, boolean activo) {
        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));
        sede.setActivo(activo);
        Sede guardada = sedeRepository.save(sede);
        log.info("Sede {} {} exitosamente", codigoSede, activo ? "activada" : "desactivada");
        return convertirADto(guardada);
    }

    @Transactional(readOnly = true)
    public SedeDto obtenerSedePorCodigo(String codigoSede) {
        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));
        return convertirADto(sede);
    }

    @Transactional(readOnly = true)
    public List<SedeDto> listarTodasSedes() {
        return sedeRepository.findAll().stream()
            .map(this::convertirADto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SedeDto> listarSedesActivas() {
        return sedeRepository.findByActivoTrue().stream()
            .map(this::convertirADto)
            .collect(Collectors.toList());
    }

    // ── Asignación de Sedes a Usuarios ──────────────────────────────────────

    /**
     * Asigna una sede a un usuario. Si ya está asignada, no hace nada (idempotente).
     */
    public UsuarioSedeDto asignarSedeAUsuario(String username, String codigoSede) {
        String normalizedUsername = normalizarUsername(username);
        Objects.requireNonNull(codigoSede, "codigoSede es obligatorio");

        User usuario = userRepository.findByUsernameIgnoreCase(normalizedUsername)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + normalizedUsername));

        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));

        if (!sede.isActivo()) {
            throw new IllegalStateException("No se puede asignar una sede inactiva: " + codigoSede);
        }

        // Idempotente: si ya existe, devolver la asignación actual
        return usuarioSedeRepository.findByUsuarioAndSede(usuario, sede)
            .map(this::convertirUsuarioSedeADto)
            .orElseGet(() -> {
                UsuarioSede nueva = new UsuarioSede(usuario, sede);
                UsuarioSede guardada = usuarioSedeRepository.save(nueva);
                log.info("Sede {} asignada al usuario {}", codigoSede, normalizedUsername);
                return convertirUsuarioSedeADto(guardada);
            });
    }

    /**
     * Elimina permanentemente una sede del sistema.
     * Solo procede si no tiene ventas u órdenes asociadas.
     */
    public void eliminarSede(String codigoSede) {
        Objects.requireNonNull(codigoSede, "codigoSede es obligatorio");
        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));
        // Primero eliminamos las asignaciones de usuarios a esta sede
        usuarioSedeRepository.deleteBySede(sede);
        sedeRepository.delete(sede);
        log.info("Sede eliminada: {}", codigoSede);
    }

    /**
     * Remueve el acceso de un usuario a una sede específica.
     */
    public void removerSedeDeUsuario(String username, String codigoSede) {
        String normalizedUsername = normalizarUsername(username);
        Objects.requireNonNull(codigoSede, "codigoSede es obligatorio");

        User usuario = userRepository.findByUsernameIgnoreCase(normalizedUsername)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + normalizedUsername));

        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));

        usuarioSedeRepository.deleteByUsuarioAndSede(usuario, sede);
        log.info("Sede {} removida del usuario {}", codigoSede, normalizedUsername);
    }

    /**
     * Lista todas las sedes asignadas a un usuario.
     */
    @Transactional(readOnly = true)
    public List<UsuarioSedeDto> listarSedesDeUsuario(String username) {
        return usuarioSedeRepository.findByUsuarioUsername(normalizarUsername(username)).stream()
            .map(this::convertirUsuarioSedeADto)
            .collect(Collectors.toList());
    }

    /**
     * Lista los usuarios con acceso a una sede específica.
     */
    @Transactional(readOnly = true)
    public List<UsuarioSedeDto> listarUsuariosDeSede(String codigoSede) {
        Sede sede = sedeRepository.findById(codigoSede.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));
        return usuarioSedeRepository.findBySede(sede).stream()
            .map(this::convertirUsuarioSedeADto)
            .collect(Collectors.toList());
    }

    /**
     * Lista las sedes activas a las que tiene acceso el usuario autenticado.
     * Se usa en el frontend para mostrar solo las opciones válidas.
     */
    @Transactional(readOnly = true)
    public List<SedeDto> obtenerSedesPermitidasParaUsuario(String username) {
        return usuarioSedeRepository.findSedesActivasByUsuarioUsername(normalizarUsername(username)).stream()
            .map(this::convertirADto)
            .collect(Collectors.toList());
    }

    /**
     * Valida que un usuario tenga acceso operativo a una sede.
     * Lanza AccessDeniedException si no tiene acceso.
     */
    public void validarAccesoUsuarioASede(String username, String codigoSede) {
        boolean tieneAcceso = usuarioSedeRepository.existsByUsuarioUsernameAndCodigoSede(
            normalizarUsername(username), codigoSede.toUpperCase());
        if (!tieneAcceso) {
            log.warn("Acceso denegado: usuario '{}' intentó acceder a sede '{}' sin autorización",
                username, codigoSede);
            throw new org.springframework.security.access.AccessDeniedException(
                "No tienes acceso a la sede: " + codigoSede);
        }
        log.debug("Acceso validado: usuario '{}' → sede '{}'", username, codigoSede);
    }

    /**
     * Verifica si un usuario tiene acceso a una sede (sin lanzar excepción).
     */
    @Transactional(readOnly = true)
    public boolean usuarioTieneAccesoASede(String username, String codigoSede) {
        return usuarioSedeRepository.existsByUsuarioUsernameAndCodigoSede(
            normalizarUsername(username), codigoSede.toUpperCase());
    }

    private String normalizarUsername(String username) {
        return Objects.requireNonNull(username, "username es obligatorio").trim();
    }

    // ── Conversores ─────────────────────────────────────────────────────────

    public SedeDto convertirADto(Sede sede) {
        if (sede == null) return null;
        SedeDto dto = new SedeDto();
        dto.setCodigoSede(sede.getCodigoSede());
        dto.setNombre(sede.getNombre());
        dto.setDireccion(sede.getDireccion());
        dto.setCiudad(sede.getCiudad());
        dto.setDepartamento(sede.getDepartamento());
        dto.setTelefono(sede.getTelefono());
        dto.setEmail(sede.getEmail());
        dto.setActivo(sede.isActivo());
        dto.setPrefijoVentas(sede.getPrefijoCodigoVenta());
        dto.setPrefijoOrdenes(sede.getPrefijoCodigoOrden());
        dto.setConsecutivoVentas(sede.getConsecutivoVentas());
        dto.setConsecutivoOrdenes(sede.getConsecutivoOrdenes());
        dto.setFechaCreacion(sede.getFechaCreacion());
        dto.setFechaActualizacion(sede.getFechaActualizacion());
        return dto;
    }

    private UsuarioSedeDto convertirUsuarioSedeADto(UsuarioSede us) {
        String nombreUsuario = "";
        if (us.getUsuario() != null) {
            String fn = us.getUsuario().getFirstName() != null ? us.getUsuario().getFirstName() : "";
            String ln = us.getUsuario().getLastName() != null ? us.getUsuario().getLastName() : "";
            nombreUsuario = (fn + " " + ln).trim();
        }
        return new UsuarioSedeDto(
            us.getId(),
            us.getUsuario() != null ? us.getUsuario().getUsername() : null,
            nombreUsuario,
            us.getSede() != null ? us.getSede().getCodigoSede() : null,
            us.getSede() != null ? us.getSede().getNombre() : null,
            us.getSede() != null ? us.getSede().getCiudad() : null
        );
    }
}
