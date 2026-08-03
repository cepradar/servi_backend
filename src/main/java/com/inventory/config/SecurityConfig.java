package com.inventory.config;

import com.inventory.util.JwtFilter;
import com.inventory.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final String[] allowedOriginPatterns;

    public SecurityConfig(@Value("${app.cors.allowed-origin-patterns:http://localhost:5173,http://127.0.0.1:5173,http://192.168.*:5173,http://10.*:5173,https://*.trycloudflare.com}") String patterns) {
        this.allowedOriginPatterns = patterns.split("\\s*,\\s*");
    }

    // Se elimina la inyección de JwtFilter a través del constructor
    // No se necesita un campo final para JwtFilter aquí

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioService userService) {
        // Spring inyectará automáticamente UsuarioService aquí, ya que es un @Service
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Utiliza el bean PasswordEncoder definido en esta clase
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter, UsuarioService usuarioService) throws Exception { // AHORA JwtFilter es un parámetro aquí
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOriginPatterns(Arrays.asList(allowedOriginPatterns));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))
            .authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/auth/register", "/auth/register-client", "/auth/login", "/api/public/**").permitAll()
    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/company/info", "/api/company/*/logo", "/api/company/*/logo2").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/company/*/logo", "/api/company/*/logo2").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/api/company/crear").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT, "/api/company/**").hasRole("ADMIN")
    .requestMatchers("/auth/update-profile-picture", "/auth/update-password", "/auth/logout").authenticated()

    // Requiere el rol ADMIN para configuración crítica
    .requestMatchers(HttpMethod.GET, "/api/categories/listarCategoria").hasAnyRole("ADMIN", "CLIENTE", "TECNICO")
    .requestMatchers(HttpMethod.GET, "/api/products/listar").hasAnyRole("ADMIN", "CLIENTE", "TECNICO")
    .requestMatchers(HttpMethod.GET, "/api/servicios/activos").authenticated()  // Lista activos: cualquier usuario autenticado
    .requestMatchers(HttpMethod.GET, "/api/servicios/**").hasAnyRole("ADMIN", "TECNICO")  // Listar/leer: admin y técnico
    .requestMatchers("/api/servicios/**").hasRole("ADMIN")  // Crear/editar/eliminar: solo admin
    .requestMatchers("/api/products/**", "/api/categories/**", "/products/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/api/auditoria/registrar").authenticated() // Permitir registro de eventos a usuarios autenticados
    .requestMatchers("/api/auditoria/**").hasAnyRole("ADMIN", "TECNICO")
    .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "TECNICO")

    // Sedes — gestión solo ADMIN, consulta de sedes propias para todos los autenticados
    .requestMatchers(HttpMethod.GET, "/api/sedes/activas").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/sedes/mis-sedes").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/sedes/**").hasAnyRole("ADMIN", "TECNICO")
    .requestMatchers("/api/sedes/**").hasRole("ADMIN")

    .requestMatchers("/api/permissions/me").authenticated()
    .requestMatchers("/api/permissions/role/**").hasRole("ADMIN")
    .requestMatchers("/api/permissions/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/api/roles/active").authenticated()
    .requestMatchers("/api/roles/**").hasRole("ADMIN")
    
    // Permite usuarios autenticados para gestión de clientes y servicios
    .requestMatchers("/api/clientes/**").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/client-categories/listar").authenticated()
    .requestMatchers("/api/cliente-electrodomestico/**").authenticated()
    .requestMatchers("/api/servicios-reparacion/**").authenticated()
    .requestMatchers("/api/marcas-electrodomestico/**").authenticated()
    .requestMatchers("/api/categorias-electrodomestico/**").authenticated()
    .requestMatchers("/api/documento-tipos/**").hasRole("ADMIN")
    .requestMatchers("/api/users/technicians").authenticated() // Para asignar técnicos en órdenes
    .requestMatchers("/api/reportes/**").authenticated() // Acceso autenticado; @PreAuthorize controla roles en cada endpoint

    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Sesión expirada o token inválido\",\"status\":401}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Acceso denegado. No tienes permisos para esta acción.\",\"status\":403}");
                })
            )
            .authenticationProvider(authenticationProvider(usuarioService)) // Pasamos el bean UsuarioService
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    
}