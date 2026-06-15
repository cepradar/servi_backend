package com.inventory.service;

import com.inventory.model.User;
import com.inventory.repository.CategoryClientRepository;
import com.inventory.repository.ClienteRepository;
import com.inventory.repository.DocumentoTipoRepository;
import com.inventory.repository.RolesRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RolesRepository roleRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CategoryClientRepository categoryClientRepository;
    @Mock
    private DocumentoTipoRepository documentoTipoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void updatePasswordActualizaCuandoLaContrasenaActualCoincide() {
        User user = new User();
        user.setUsername("TECNICO");
        user.setPassword("encoded-old");

        when(userRepository.findByUsernameIgnoreCase("TECNICO")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Anterior123", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("encoded-new");

        Boolean updated = usuarioService.updatePassword("TECNICO", "Anterior123", "NuevaClave123");

        assertTrue(updated);
        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePasswordFallaCuandoLaContrasenaActualNoCoincide() {
        User user = new User();
        user.setUsername("ADMIN");
        user.setPassword("encoded-old");

        when(userRepository.findByUsernameIgnoreCase("ADMIN")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Incorrecta123", "encoded-old")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.updatePassword("ADMIN", "Incorrecta123", "NuevaClave123"));

        assertEquals("La contraseña actual no es correcta", exception.getMessage());
    }
}
