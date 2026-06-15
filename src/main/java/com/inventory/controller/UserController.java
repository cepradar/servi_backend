package com.inventory.controller;

import com.inventory.dto.ClientRegisterRequest;
import com.inventory.dto.UpdatePswUserDto;
import com.inventory.dto.UserDto;
import com.inventory.dto.LoginRequest;
import com.inventory.dto.RegisterRequest;
import com.inventory.model.Rol;
import com.inventory.model.User;
import com.inventory.service.UsuarioService;
import com.inventory.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UsuarioService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User registerUser(@RequestBody RegisterRequest registerRequest) {
        //Convertir la solicitud a Dto
        UpdatePswUserDto actualizarPSWUsuarioDto = new UpdatePswUserDto(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getRole());
        // Registrar un nuevo usuario con nombre de usuario, contraseña y rol
        return userService.registerUser(actualizarPSWUsuarioDto);
    }

    @PostMapping("/register-client")
    public ResponseEntity<?> registerClient(@RequestBody ClientRegisterRequest request) {
        try {
            // ── Validaciones ────────────────────────────────────────────────
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico es obligatorio"));
            }
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico no es válido"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
            }
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio"));
            }
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El apellido es obligatorio"));
            }
            if (request.getNit() == null || request.getNit().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El número de documento es obligatorio"));
            }
            if (!request.getNit().trim().matches("^[A-Za-z0-9\\-]{3,20}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "El número de documento no es válido"));
            }

            User newClient = userService.registerClient(
                request.getEmail().trim().toLowerCase(),
                request.getPassword(),
                request.getFirstName().trim(),
                request.getLastName().trim(),
                request.getTelefono(),
                request.getNit().trim(),
                request.getTipoDocumento(),
                request.getDireccion() != null ? request.getDireccion().trim() : null
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cliente registrado exitosamente");
            response.put("username", newClient.getUsername());
            response.put("email", newClient.getEmail());
            response.put("fullName", newClient.getFirstName() + " " + newClient.getLastName());

            logger.info("Nuevo cliente registrado: {}", newClient.getEmail());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al registrar cliente: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error inesperado al registrar cliente: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar el registro. Intente nuevamente.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) throws Exception {
        String normalizedUsername = loginRequest.getUsername() != null ? loginRequest.getUsername().trim() : null;

        // Log para imprimir el cuerpo de la solicitud de login
        logger.info("Cuerpo de la solicitud de login: username={}, password={}",
                normalizedUsername, loginRequest.getPassword());

        Optional<UserDto> user;
        try {
            // Autenticación del usuario
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedUsername, loginRequest.getPassword()));

            // Recuperar el usuario autenticado de la base de datos
            user = userService.findByUsername(normalizedUsername);
            if (user.isEmpty()) {
                throw new Exception("Usuario no encontrado");
            }

        } catch (AuthenticationException e) {
            throw new Exception("Credenciales inválidas: " + e.getMessage());
        }

        // Obtener el rol del usuario autenticado
        String role = user.get().getRole(); // UserDto.getRole() ya devuelve un String

        // Generar token JWT para el usuario autenticado
        String token = jwtUtil.generateToken(user.get().getUsername(), role);

        // Log del token generado
        logger.info("Token generado para el usuario {}: {}", user.get().getUsername(), token);

        return buildAuthResponse(user.get(), token, "Inicio de sesión exitoso");
    }

    @PostMapping("/validate")
    public String validateToken(@RequestBody String token) {
        // Validación del token para asegurar que no ha expirado y que el rol es
        // adecuado
        if (jwtUtil.isTokenExpired(token)) {
            return "Token expirado";
        }

        // Aquí podrías hacer una validación adicional de roles
        if (!jwtUtil.hasRole(token, "ADMIN")) {
            return "Acceso denegado: rol no autorizado";
        }

        return "Token válido";
    }

    @RequestMapping(value = {"/change-password", "/update-password"}, method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request,
                                                              Authentication authentication,
                                                              HttpServletRequest httpRequest) {
        String username = resolveUsername(authentication, request.get("token"), httpRequest);
        String currentPassword = firstNonBlank(request.get("currentPassword"), request.get("oldPassword"));
        String newPassword = firstNonBlank(request.get("newPassword"), request.get("password"));

        boolean isUpdated = userService.updatePassword(username, currentPassword, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message",
                isUpdated ? "Contraseña actualizada correctamente" : "Error al actualizar la contraseña");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No hay un usuario autenticado"));
        }

        return userService.findByUsername(authentication.getName())
                .<ResponseEntity<?>>map(userDto -> ResponseEntity.ok(buildAuthResponse(userDto, null, "Perfil obtenido correctamente")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado")));
    }

    @PostMapping("/update-profile-picture")
    public Map<String, String> updateProfilePicture(@RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {
        // Extraer el token del header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Token no encontrado o formato inválido");
        }
        
        String token = authHeader.substring(7); // Elimina "Bearer "
        String username = jwtUtil.extractUsername(token);
        boolean isUpdated = userService.updateProfilePicture(username, file);

        Map<String, String> response = new HashMap<>();
        response.put("message",
                isUpdated ? "Foto de perfil actualizada correctamente" : "Error al actualizar la foto de perfil");
        return response;
    }

    @GetMapping("/profile-picture/{username}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String username) {
        var picture = userService.getProfilePicture(username);
        
        if (picture.isPresent() && picture.get().length > 0) {
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(picture.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        // Lógica de cierre de sesión si es necesaria (como invalidar el token)
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesión cerrada correctamente");
        return response;
    }

    private Map<String, String> buildAuthResponse(UserDto user, String token, String message) {
        Map<String, String> response = new HashMap<>();
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = user.getFullName() != null ? user.getFullName() : (firstName + " " + lastName).trim();
        String displayName = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : (!fullName.isBlank() ? fullName : user.getUsername());

        if (token != null) {
            response.put("token", token);
        }
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("firstName", firstName);
        response.put("lastName", lastName);
        response.put("fullName", fullName);
        response.put("displayName", displayName);
        response.put("name", displayName);
        response.put("email", user.getEmail() != null ? user.getEmail() : "");
        response.put("message", message);
        return response;
    }

    private String resolveUsername(Authentication authentication, String tokenFromBody, HttpServletRequest request) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }

        String token = firstNonBlank(tokenFromBody, extractBearerToken(request));
        if (token == null) {
            throw new IllegalArgumentException("No fue posible identificar al usuario autenticado");
        }
        return jwtUtil.extractUsername(token);
    }

    private String extractBearerToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String firstNonBlank(String primary, String secondary) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (secondary != null && !secondary.isBlank()) {
            return secondary;
        }
        return null;
    }

}