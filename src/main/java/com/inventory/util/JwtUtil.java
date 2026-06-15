package com.inventory.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    // La clave secreta se inyecta desde las propiedades de configuración
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * Genera un token JWT para un usuario.
     *
     * @param username el nombre de usuario
     * @param roles los roles del usuario
     * @return el token generado
     */
    public String generateToken(String username, String role) {
        if (username == null || role == null) {
            throw new IllegalArgumentException("El nombre de usuario y el rol no pueden ser nulos");
        }
        Key key = getSigningKey();

        String token = Jwts.builder()
        .setSubject(username)
        .claim("role", role) // Agregar el rol como claim
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();
        // Generar el token con username y role
        return token;
             
        }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token inválido: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extrae el nombre de usuario del token.
     *
     * @param token el token JWT
     * @return el nombre de usuario
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extrae los roles del token.
     *
     * @param token el token JWT
     * @return los roles del usuario
     */
    public String extractRoles(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Verifica si un token ha expirado.
     *
     * @param token el token JWT
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (SignatureException e) {
            throw new IllegalArgumentException("No se puede verificar la firma del token", e);
        } catch (JwtException e) {
            throw new IllegalArgumentException("No se puede verificar la expiración del token.", e);
        }
    }
    

    /**
     * Valida un token JWT comparándolo con los detalles del usuario.
     *
     * @param token       el token JWT
     * @param userDetails los detalles del usuario
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Verifica si el token tiene un rol específico.
     *
     * @param token el token JWT
     * @param role el rol que se desea verificar
     * @return true si el token tiene el rol, false en caso contrario
     */
    public boolean hasRole(String token, String role) {
        String tokenRole = extractRoles(token);
        return tokenRole != null && tokenRole.equalsIgnoreCase(role);
    }

    private Key getSigningKey() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET no está configurado");
        }

        byte[] keyBytes = secretKey.trim().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 64 caracteres ASCII para usar HS512. Longitud actual: "
                            + keyBytes.length);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
     
}
