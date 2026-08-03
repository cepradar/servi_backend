package com.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;

    public WebConfig(@Value("${app.cors.allowed-origin-patterns:http://localhost:5173,http://127.0.0.1:5173,http://192.168.*:5173,http://10.*:5173,https://*.trycloudflare.com}") String patterns) {
        this.allowedOriginPatterns = patterns.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(allowedOriginPatterns)  // Permitir solicitudes desde el frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos permitidos
                .allowedHeaders("*")
                .allowCredentials(true);  // Permitir todos los encabezados
    }
}
