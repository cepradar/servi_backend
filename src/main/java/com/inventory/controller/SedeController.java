package com.inventory.controller;

import com.inventory.dto.SedeDto;
import com.inventory.dto.SedeRegistroDto;
import com.inventory.dto.UsuarioSedeDto;
import com.inventory.service.SedeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private static final Logger log = LoggerFactory.getLogger(SedeController.class);

    @Autowired
    private SedeService sedeService;

    // ── CRUD Sedes (solo ADMIN) ──────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearSede(@Valid @RequestBody SedeRegistroDto dto) {
        try {
            SedeDto sede = sedeService.crearSede(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(sede);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear sede: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{codigoSede}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarSede(@PathVariable String codigoSede,
                                             @Valid @RequestBody SedeRegistroDto dto) {
        try {
            SedeDto sede = sedeService.actualizarSede(codigoSede, dto);
            return ResponseEntity.ok(sede);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{codigoSede}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarSede(@PathVariable String codigoSede) {
        try {
            sedeService.eliminarSede(codigoSede);
            return ResponseEntity.ok(Map.of("mensaje", "Sede eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{codigoSede}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activarSede(@PathVariable String codigoSede) {
        try {
            SedeDto sede = sedeService.cambiarEstadoSede(codigoSede, true);
            return ResponseEntity.ok(sede);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{codigoSede}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivarSede(@PathVariable String codigoSede) {
        try {
            SedeDto sede = sedeService.cambiarEstadoSede(codigoSede, false);
            return ResponseEntity.ok(sede);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Alterna el estado activo/inactivo de una sede.
     * Usado desde el panel de administración de sedes del frontend.
     */
    @PatchMapping("/{codigoSede}/toggle-activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleActivoSede(@PathVariable String codigoSede) {
        try {
            SedeDto actual = sedeService.obtenerSedePorCodigo(codigoSede);
            SedeDto resultado = sedeService.cambiarEstadoSede(codigoSede, !actual.isActivo());
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{codigoSede}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerSede(@PathVariable String codigoSede) {
        try {
            SedeDto sede = sedeService.obtenerSedePorCodigo(codigoSede);
            return ResponseEntity.ok(sede);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SedeDto>> listarTodasSedes() {
        return ResponseEntity.ok(sedeService.listarTodasSedes());
    }

    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SedeDto>> listarSedesActivas() {
        return ResponseEntity.ok(sedeService.listarSedesActivas());
    }

    // ── Sedes permitidas para el usuario autenticado ─────────────────────────

    /**
     * Devuelve las sedes a las que tiene acceso el usuario autenticado.
     * Solo devuelve sedes activas asignadas explícitamente en usuario_sede.
     */
    @GetMapping("/mis-sedes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SedeDto>> misSedes(Authentication auth) {
        return ResponseEntity.ok(sedeService.obtenerSedesPermitidasParaUsuario(auth.getName()));
    }

    // ── Gestión de Usuarios por Sede ─────────────────────────────────────────

    @PostMapping("/{codigoSede}/usuarios/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignarUsuarioASede(@PathVariable String codigoSede,
                                                  @PathVariable String username) {
        try {
            UsuarioSedeDto asignacion = sedeService.asignarSedeAUsuario(username, codigoSede);
            return ResponseEntity.status(HttpStatus.CREATED).body(asignacion);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{codigoSede}/usuarios/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removerUsuarioDeSede(@PathVariable String codigoSede,
                                                   @PathVariable String username) {
        try {
            sedeService.removerSedeDeUsuario(username, codigoSede);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario removido de la sede exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{codigoSede}/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioSedeDto>> listarUsuariosDeSede(@PathVariable String codigoSede) {
        return ResponseEntity.ok(sedeService.listarUsuariosDeSede(codigoSede));
    }

    // ── Gestión de Sedes por Usuario ─────────────────────────────────────────

    @PostMapping("/usuarios/{username}/sedes/{codigoSede}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignarSedeAUsuario(@PathVariable String username,
                                                  @PathVariable String codigoSede) {
        try {
            UsuarioSedeDto asignacion = sedeService.asignarSedeAUsuario(username, codigoSede);
            return ResponseEntity.status(HttpStatus.CREATED).body(asignacion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/usuarios/{username}/sedes/{codigoSede}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removerSedeDeUsuario(@PathVariable String username,
                                                   @PathVariable String codigoSede) {
        try {
            sedeService.removerSedeDeUsuario(username, codigoSede);
            return ResponseEntity.ok(Map.of("mensaje", "Sede removida del usuario exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usuarios/{username}/sedes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioSedeDto>> listarSedesDeUsuario(@PathVariable String username) {
        return ResponseEntity.ok(sedeService.listarSedesDeUsuario(username));
    }
}
