package com.inventory.service;

import com.inventory.dto.UpdatePswUserDto;
import com.inventory.dto.UserDto;
import com.inventory.model.CategoryClient;
import com.inventory.model.Cliente;
import com.inventory.model.DocumentoTipo;
import com.inventory.model.Rol;
import com.inventory.model.User;
import com.inventory.repository.CategoryClientRepository;
import com.inventory.repository.ClienteRepository;
import com.inventory.repository.DocumentoTipoRepository;
import com.inventory.repository.RolesRepository;
import com.inventory.repository.UserRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

@Service
public class UsuarioService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolesRepository roleRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoryClientRepository categoryClientRepository;

    @Autowired
    private DocumentoTipoRepository documentoTipoRepository;

    public User registerUser(UpdatePswUserDto actualizarUsuariosDto) {
        // Verificamos si el rol existe por nombre (usando el DTO para obtener el nombre)
        Rol role = roleRepository.findByName(actualizarUsuariosDto.getRole().getName());
        if (role == null) {
            throw new IllegalArgumentException("El rol especificado no existe");
        }

        User user = UpdatePswUserDto.toUsuarios(actualizarUsuariosDto);
        // Aquí debes cifrar la contraseña antes de guardar el usuario
        user.setPassword(passwordEncoder.encode(actualizarUsuariosDto.getNewPassword())); // Aquí se cifra la contraseña

        return userRepository.save(user);
    }

    public Optional<UserDto> findByUsername(String username) {
        String normalizedUsername = normalizeUsername(username);
        Optional<User> usuarios = userRepository.findByUsernameIgnoreCase(normalizedUsername);

        // Si se encuentra, lo convertimos a UsuarioDto y lo devolvemos
        return usuarios.map(UserDto::new);
    }

    public List<UserDto> obtenerUsuarios() {
        // Obtenemos todos los categorias desde la base de datos
        List<User> usuarios = userRepository.findAll();

        // Convertimos la lista de usuarios a una lista de UsuariosDto
        return usuarios.stream()
                .map(UserDto::new) // Convierte cada usuario en UsuariosDto
                .collect(Collectors.toList());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = normalizeUsername(username);
        User user = userRepository.findByUsernameWithoutPictureIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el nombre: " + username));
        return user; // User implementa UserDetails
    }

    public Boolean updateProfilePicture(String username, MultipartFile file) throws IOException {
        // Verificar que el usuario existe
        String normalizedUsername = normalizeUsername(username);
        if (!userRepository.findByUsernameWithoutPictureIgnoreCase(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        // Guardar la foto SOLO en la BD sin cargar el usuario completo
        byte[] fileBytes = compressProfileImage(file.getBytes(), 256, 0.8f);
        userRepository.updateProfilePictureByUsername(fileBytes, normalizedUsername);
        return true;
    }

    public Optional<byte[]> getProfilePicture(String username) {
        return userRepository.findProfilePictureByUsername(normalizeUsername(username));
    }

    public Boolean updatePassword(UpdatePswUserDto usuarioDto) {
        return updatePassword(usuarioDto.getUsername(), null, usuarioDto.getNewPassword());
    }

    public Boolean updatePassword(String username, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        User user = userOptional.get();
        if (currentPassword != null && !currentPassword.isBlank()
                && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("La nueva contraseña no cumple con los requisitos de seguridad");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    private boolean isValidPassword(String password) {
        // Lógica para validar la contraseña
        return password != null && password.length() >= 8; // Ejemplo de validación
    }

    public User createUser(UserDto userDto) {
        // Verificar que el usuario no exista
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        if (userDto.getRole() != null) {
            Rol role = roleRepository.findByName(userDto.getRole());
            user.setRole(role);
        }
        user.setProfilePicture(userDto.getProfilePicture());
        
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String username) {
        return userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
    }

    public User updateUser(String username, UserDto userDto) {
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userDto.getRole() != null) {
                Rol role = roleRepository.findByName(userDto.getRole());
                user.setRole(role);
            }
            if (userDto.getProfilePicture() != null) {
                user.setProfilePicture(userDto.getProfilePicture());
            }
            if (userDto.getFirstName() != null) {
                user.setFirstName(userDto.getFirstName());
            }
            if (userDto.getLastName() != null) {
                user.setLastName(userDto.getLastName());
            }
            if (userDto.getEmail() != null) {
                user.setEmail(userDto.getEmail());
            }
            if (userDto.getTelefono() != null) {
                user.setTelefono(userDto.getTelefono());
            }
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("Usuario no encontrado");
    }

    public Boolean deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
            return true;
        }
        throw new IllegalArgumentException("Usuario no encontrado");
    }

    public List<UserDto> obtenerTecnicos() {
        // Obtenemos todos los usuarios y filtramos por rol TECNICO
        List<User> usuarios = userRepository.findAll();

        // Convertimos la lista de usuarios a una lista de UsuariosDto y filtramos por rol TECNICO
        return usuarios.stream()
                .filter(user -> user.getRole() != null && "TECNICO".equalsIgnoreCase(user.getRole().getName()))
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        return username.trim();
    }

    private byte[] compressProfileImage(byte[] originalBytes, int maxSize, float quality) throws IOException {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (original == null) {
            return originalBytes;
        }

        int width = original.getWidth();
        int height = original.getHeight();
        double scale = Math.min(1.0, (double) maxSize / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        Image scaled = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        if (params.canWriteCompressed()) {
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(output, null, null), params);
        } finally {
            writer.dispose();
        }

        return out.toByteArray();
    }

    /**
     * Registra un nuevo cliente desde el landing page.
     * Crea el registro en la tabla {@code usuarios} (autenticación) y en
     * la tabla {@code clientes} (entidad de negocio) de forma atómica.
     */
    @Transactional
    public User registerClient(String email, String password, String firstName, String lastName,
                               String telefono, String nit, String tipoDocumentoId,
                               String direccion) {
        // ── 1. Validar unicidad de email/username ────────────────────────────
        if (userRepository.findByUsername(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este correo electrónico");
        }

        // ── 2. Validar unicidad del documento en la tabla clientes ───────────
        String tdId = (tipoDocumentoId != null && !tipoDocumentoId.isBlank()) ? tipoDocumentoId.toUpperCase() : "CC";
        if (clienteRepository.existsByNitAndTipoDocumentoId(nit, tdId)) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con ese número de documento");
        }

        // ── 3. Resolver dependencias ─────────────────────────────────────────
        Rol clientRole = roleRepository.findByName("CLIENTE");
        if (clientRole == null) {
            throw new IllegalStateException("El rol CLIENTE no existe en el sistema. Contacte al administrador.");
        }

        DocumentoTipo tipoDoc = documentoTipoRepository.findById(tdId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento '" + tdId + "' no válido"));

        CategoryClient categoriaParticular = categoryClientRepository.findById("PART")
                .orElseThrow(() -> new IllegalStateException("Categoría de cliente PART no encontrada"));

        // ── 4. Crear usuario (tabla usuarios) ────────────────────────────────
        User newUser = new User();
        newUser.setUsername(email);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setTelefono(telefono);
        newUser.setRole(clientRole);
        userRepository.save(newUser);

        // ── 5. Crear cliente (tabla clientes) ────────────────────────────────
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNit(nit);
        nuevoCliente.setCategory(categoriaParticular);
        nuevoCliente.setTipoDocumento(tipoDoc);
        nuevoCliente.setNombre(firstName);
        nuevoCliente.setApellido(lastName);
        nuevoCliente.setTelefono(telefono);
        nuevoCliente.setDireccion(direccion);
        nuevoCliente.setActivo(true);
        nuevoCliente.setEmail(email);
        clienteRepository.save(nuevoCliente);

        return newUser;
    }
}
