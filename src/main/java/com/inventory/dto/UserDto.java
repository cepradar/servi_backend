package com.inventory.dto;

import com.inventory.model.User;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDto {

    private String id;
    private String username;
    private String email;
    private String telefono;
    private String firstName;
    private String lastName;
    private String fullName;
    private String displayName;
    private String role;
    private String roleColor;
    private byte[] profilePicture;
    private String cedula;
    private boolean activo;
    private boolean cuentaBloqueada;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;

    // Constructor
    public UserDto(User usuario) {
        this.id = usuario.getUsername();
        this.username = usuario.getUsername();
        this.role = usuario.getRole() != null ? usuario.getRole().getName() : "USER";
        this.roleColor = usuario.getRole() != null && usuario.getRole().getColor() != null ? usuario.getRole().getColor() : "#2563eb";
        this.profilePicture = usuario.getProfilePicture();
        this.firstName = usuario.getFirstName();
        this.lastName = usuario.getLastName();
        this.fullName = buildFullName(usuario.getFirstName(), usuario.getLastName());
        this.displayName = !this.fullName.isBlank() ? this.fullName : usuario.getUsername();
        this.email = usuario.getEmail();
        this.telefono = usuario.getTelefono();
        this.cedula = usuario.getCedula();
        this.activo = usuario.isActivo();
        this.cuentaBloqueada = usuario.isCuentaBloqueada();
        this.fechaCreacion = usuario.getFechaCreacion();
        this.ultimoLogin = usuario.getUltimoLogin();
    }

    @Override
    public String toString() {
        return "UsuarioDto{username=" + username + "', roleName='" + role + "', profilePicture='" + profilePicture + "'}";
    }

    public static User toUsuarios(UserDto usuariosDto) {
        User usuarios = new User();
        usuarios.setUsername(usuariosDto.getUsername());
        usuarios.setProfilePicture(usuariosDto.getProfilePicture());
        usuarios.setFirstName(usuariosDto.getFirstName());
        usuarios.setLastName(usuariosDto.getLastName());
        usuarios.setEmail(usuariosDto.getEmail());
        usuarios.setTelefono(usuariosDto.getTelefono());
        if (usuariosDto.getCedula() != null) usuarios.setCedula(usuariosDto.getCedula());
        return usuarios;
    }

    private static String buildFullName(String firstName, String lastName) {
        String safeFirstName = firstName != null ? firstName.trim() : "";
        String safeLastName = lastName != null ? lastName.trim() : "";
        return (safeFirstName + " " + safeLastName).trim();
    }
}
