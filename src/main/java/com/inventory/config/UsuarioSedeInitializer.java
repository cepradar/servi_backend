package com.inventory.config;

import com.inventory.model.Sede;
import com.inventory.model.User;
import com.inventory.model.UsuarioSede;
import com.inventory.repository.SedeRepository;
import com.inventory.repository.UserRepository;
import com.inventory.repository.UsuarioSedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class UsuarioSedeInitializer implements CommandLineRunner {

    private static final String SEDE_INICIAL = "SP";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private UsuarioSedeRepository usuarioSedeRepository;

    @Override
    public void run(String... args) {
        Sede sede = sedeRepository.findById(SEDE_INICIAL).orElse(null);
        if (sede == null) {
            return;
        }

        assignSedeIfMissing("ADMIN", sede);
        assignSedeIfMissing("TECNICO", sede);
    }

    private void assignSedeIfMissing(String username, Sede sede) {
        User user = userRepository.findById(username).orElse(null);
        if (user == null || usuarioSedeRepository.findByUsuarioAndSede(user, sede).isPresent()) {
            return;
        }

        usuarioSedeRepository.save(new UsuarioSede(user, sede));
    }
}
