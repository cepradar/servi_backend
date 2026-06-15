package com.inventory.config;

import com.inventory.model.Rol;
import com.inventory.model.User;
import com.inventory.repository.RolesRepository;
import com.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class AdminUserInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String ADMIN_PASSWORD = "CANEYA";
    private static final String TECNICO_USERNAME = "TECNICO";
    private static final String TECNICO_PASSWORD = "TECNICO";

    @Autowired private UserRepository userRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Rol adminRole = ensureRole("ADMIN", "#4f46e5", "Administrador del sistema");
        Rol tecnicoRole = ensureRole("TECNICO", "#16a34a", "Tecnico de servicio");

        createUserIfMissing(ADMIN_USERNAME, ADMIN_PASSWORD, adminRole, "Admin", "Inicial", "admin@local");
        createUserIfMissing(TECNICO_USERNAME, TECNICO_PASSWORD, tecnicoRole, "Tecnico", "Inicial", "tecnico@local");
    }

    private Rol ensureRole(String roleName, String color, String description) {
        Rol role = rolesRepository.findByName(roleName);
        if (role == null) {
            role = rolesRepository.save(new Rol(roleName, color, description));
        }
        return role;
    }

    private void createUserIfMissing(String username, String rawPassword, Rol role,
                                     String firstName, String lastName, String email) {
        if (userRepository.existsById(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        userRepository.save(user);
    }
}
