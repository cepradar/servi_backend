package com.inventory.controller;

import com.inventory.dto.LoginRequest;
import com.inventory.dto.UserDto;
import com.inventory.model.Rol;
import com.inventory.model.User;
import com.inventory.service.UsuarioService;
import com.inventory.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void loginIncluyeDatosDePerfilEnLaRespuesta() throws Exception {
        UserController controller = new UserController();
        UsuarioService usuarioService = mock(UsuarioService.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);

        ReflectionTestUtils.setField(controller, "userService", usuarioService);
        ReflectionTestUtils.setField(controller, "authenticationManager", authenticationManager);
        ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);

        User user = new User();
        user.setUsername("TECNICO");
        user.setFirstName("Carlos");
        user.setLastName("Prada");
        user.setEmail("tecnico@local");
        user.setRole(new Rol("TECNICO"));

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioService.findByUsername("TECNICO")).thenReturn(Optional.of(new UserDto(user)));
        when(jwtUtil.generateToken("TECNICO", "TECNICO")).thenReturn("jwt-token");

        LoginRequest request = new LoginRequest();
        request.setUsername("TECNICO");
        request.setPassword("TECNICO");

        Map<String, String> response = controller.login(request);

        assertEquals("jwt-token", response.get("token"));
        assertEquals("Carlos", response.get("firstName"));
        assertEquals("Prada", response.get("lastName"));
        assertEquals("Carlos Prada", response.get("fullName"));
        assertEquals("Carlos Prada", response.get("displayName"));
        assertEquals("Carlos Prada", response.get("name"));
    }

    @Test
    void getCurrentUserProfileDevuelveElPerfilAutenticado() {
        UserController controller = new UserController();
        UsuarioService usuarioService = mock(UsuarioService.class);

        ReflectionTestUtils.setField(controller, "userService", usuarioService);

        User user = new User();
        user.setUsername("ADMIN");
        user.setFirstName("Admin");
        user.setLastName("Inicial");
        user.setRole(new Rol("ADMIN"));

        when(usuarioService.findByUsername("ADMIN")).thenReturn(Optional.of(new UserDto(user)));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("ADMIN");

        ResponseEntity<?> response = controller.getCurrentUserProfile(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Admin Inicial", body.get("displayName"));
        assertTrue(body.containsKey("fullName"));
    }

    @Test
    void changePasswordResuelveUsuarioDesdeAuthorizationHeader() {
        UserController controller = new UserController();
        UsuarioService usuarioService = mock(UsuarioService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ReflectionTestUtils.setField(controller, "userService", usuarioService);
        ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);

        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(jwtUtil.extractUsername("jwt-token")).thenReturn("TECNICO");
        when(usuarioService.updatePassword("TECNICO", "Anterior123", "NuevaClave123")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = controller.changePassword(
                Map.of("currentPassword", "Anterior123", "newPassword", "NuevaClave123"),
                null,
                request
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Contraseña actualizada correctamente", response.getBody().get("message"));
    }
}
