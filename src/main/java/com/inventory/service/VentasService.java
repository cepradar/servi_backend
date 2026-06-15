package com.inventory.service;

import com.inventory.dto.VentaDto;
import com.inventory.dto.VentaDetalleDto;
import com.inventory.dto.VentaDetalleRegistroDto;
import com.inventory.dto.VentaRegistroDto;
import com.inventory.model.Cliente;
import com.inventory.model.Product;
import com.inventory.model.Sede;
import com.inventory.model.User;
import com.inventory.model.Venta;
import com.inventory.model.VentaDetalle;
import com.inventory.model.OrdenDeServicio;
import com.inventory.repository.ClienteRepository;
import com.inventory.repository.SedeRepository;
import com.inventory.repository.UsuarioSedeRepository;
import com.inventory.repository.VentaRepository;
import com.inventory.repository.VentaDetalleRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.UserRepository;
import com.inventory.repository.OrdenDeServicioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VentasService {

    private static final Logger log = LoggerFactory.getLogger(VentasService.class);

    @Autowired private VentaRepository ventaRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AuditoriaService auditoriaService;
    @Autowired private VentaDetalleRepository ventaDetalleRepository;
    @Autowired private OrdenDeServicioRepository ordenDeServicioRepository;
    @Autowired private SedeRepository sedeRepository;
    @Autowired private UsuarioSedeRepository usuarioSedeRepository;

    /**
     * Valida que el técnico autenticado tenga la orden asignada antes de registrar una venta.
     */
    public void validarAccesoOrdenParaVenta(String ordenId, String username) {
        OrdenDeServicio orden = ordenDeServicioRepository.findById(ordenId)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + ordenId));
        User tecnico = orden.getTecnicoAsignado();
        if (tecnico == null || !tecnico.getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "No tienes permiso para registrar ventas en esta orden");
        }
    }

    /**
     * Registra una nueva venta con ID generado por sede.
     *
     * Flujo thread-safe:
     * 1. Valida que el usuario tenga acceso a la sede indicada.
     * 2. Obtiene lock pesimista sobre la sede para leer/incrementar el consecutivo.
     * 3. Genera el código de venta: V-{CODIGO_SEDE}-{CONSECUTIVO_6_DIGITOS}
     * 4. Incrementa y persiste el consecutivo dentro de la misma transacción.
     * 5. Crea y guarda la venta con los detalles.
     */
    public VentaDto registrarVenta(VentaRegistroDto registroDto) {
        String codigoSede = Objects.requireNonNull(registroDto.getCodigoSede(),
            "codigoSede es obligatorio para registrar una venta").trim().toUpperCase();
        String username = Objects.requireNonNull(registroDto.getUsuarioUsername(), "usuarioUsername");

        log.info("Iniciando registro de venta — usuario: {}, sede: {}", username, codigoSede);

        // ── 1. Validar acceso del usuario a la sede ──────────────────────────
        boolean tieneAcceso = usuarioSedeRepository.existsByUsuarioUsernameAndCodigoSede(username, codigoSede);
        if (!tieneAcceso) {
            log.warn("Acceso denegado: usuario '{}' no tiene acceso a la sede '{}'", username, codigoSede);
            throw new org.springframework.security.access.AccessDeniedException(
                "No tienes acceso para registrar ventas en la sede: " + codigoSede);
        }

        // ── 2. Resolver entidades ─────────────────────────────────────────────
        User usuario = userRepository.findById(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        String clienteId     = Objects.requireNonNull(registroDto.getClienteId(), "clienteId");
        String tipoDoc        = registroDto.getClienteTipoDocumento();
        Cliente cliente;
        if (tipoDoc != null && !tipoDoc.isBlank()) {
            cliente = clienteRepository.findByNitAndTipoDocumentoId(clienteId, tipoDoc)
                .orElseThrow(() -> new RuntimeException(
                    "Cliente no encontrado: nit=" + clienteId + ", tipo=" + tipoDoc));
        } else {
            cliente = clienteRepository.findByNit(clienteId).stream().findFirst()
                .orElseThrow(() -> new RuntimeException(
                    "Cliente no encontrado: nit=" + clienteId));
        }

        if (registroDto.getDetalles() == null || registroDto.getDetalles().isEmpty()) {
            throw new RuntimeException("La venta debe incluir al menos un detalle");
        }

        // ── 3. Obtener sede con LOCK pesimista e incrementar consecutivo ─────
        Sede sede = sedeRepository.findByCodigoSedeParaActualizacion(codigoSede)
            .orElseThrow(() -> new RuntimeException("Sede no encontrada: " + codigoSede));

        if (!sede.isActivo()) {
            throw new IllegalStateException("La sede '" + codigoSede + "' está inactiva");
        }

        int consecutivo = sede.getConsecutivoVentas();
        String prefijoVenta = sede.getPrefijoCodigoVenta() != null && !sede.getPrefijoCodigoVenta().isBlank()
            ? sede.getPrefijoCodigoVenta().trim().toUpperCase()
            : "V";
        String ventaId = String.format("%s-%s-%06d", codigoSede, prefijoVenta, consecutivo);
        sede.setConsecutivoVentas(consecutivo + 1);
        sedeRepository.save(sede);

        log.info("Consecutivo venta asignado: {} (próximo: {})", ventaId, consecutivo + 1);

        // ── 4. Construir y guardar la venta ────────────────────────────────────
        Venta venta = new Venta();
        venta.setId(ventaId);
        venta.setSede(sede);
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());
        venta.setObservaciones(registroDto.getObservaciones());
        venta.setOrdenDeServicioId(registroDto.getOrdenDeServicioId());
        venta.setEstado("PAGADA");
        if (registroDto.getFormaPago() != null) venta.setFormaPago(registroDto.getFormaPago());
        if (registroDto.getDescuento() != null) venta.setDescuento(registroDto.getDescuento());
        else venta.setDescuento(java.math.BigDecimal.ZERO);

        Venta ventaGuardada = ventaRepository.save(venta);

        // ── 5. Crear detalles ────────────────────────────────────────────────
        List<VentaDetalle> detalles = new java.util.ArrayList<>();

        for (VentaDetalleRegistroDto detalleDto : registroDto.getDetalles()) {
            if (detalleDto.getProductId() == null) {
                throw new RuntimeException("productId es requerido para cada detalle de venta");
            }
            Product producto = productRepository.findById(detalleDto.getProductId())
                .orElseThrow(() -> new RuntimeException(
                    "Producto no encontrado: " + detalleDto.getProductId()));

            if (producto.getQuantity() < detalleDto.getCantidad()) {
                throw new RuntimeException("Cantidad insuficiente para '" + producto.getName()
                    + "'. Disponible: " + producto.getQuantity());
            }

            detalles.add(new VentaDetalle(ventaGuardada, producto,
                detalleDto.getCantidad(), detalleDto.getPrecioUnitario()));

            int cantidadInicial = producto.getQuantity();
            producto.setQuantity(producto.getQuantity() - detalleDto.getCantidad());
            productRepository.save(producto);

            auditoriaService.registrarMovimiento(
                producto.getId(), cantidadInicial, producto.getQuantity(),
                detalleDto.getPrecioUnitario(), detalleDto.getPrecioUnitario(),
                "VC",
                "Venta " + ventaId + " — cliente: " + cliente.getNombre() + " " + cliente.getApellido(),
                username,
                ventaId
            );
        }

        ventaGuardada.setDetalles(detalles);
        ventaRepository.save(ventaGuardada);

        log.info("Venta registrada exitosamente: {} en sede {}", ventaId, codigoSede);
        return convertirADto(ventaGuardada);
    }

    /** Obtiene todas las ventas ordenadas por fecha descendente. */
    public List<VentaDto> obtenerTodasVentas() {
        return ventaRepository.findAll().stream()
                .sorted((v1, v2) -> v2.getFecha().compareTo(v1.getFecha()))
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /** Obtiene ventas de un producto específico. */
    public List<VentaDto> obtenerVentasProducto(String productId) {
        Product producto = productRepository.findById(
                Objects.requireNonNull(productId, "productId"))
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return ventaDetalleRepository.findByProduct(producto).stream()
                .map(VentaDetalle::getVenta)
                .distinct()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /** Obtiene ventas realizadas por un usuario específico. */
    public List<VentaDto> obtenerVentasUsuario(String usuarioUsername) {
        User usuario = userRepository.findById(
                Objects.requireNonNull(usuarioUsername, "usuarioUsername"))
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ventaRepository.findByUsuario(usuario).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /** Obtiene ventas en un rango de fechas. */
    public List<VentaDto> obtenerVentasEnRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findVentasByFechaRango(fechaInicio, fechaFin).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /** Busca ventas cuyo cliente coincide (nombre o apellido) con el texto dado. */
    public List<VentaDto> obtenerVentasPorComprador(String nombre) {
        return ventaRepository.findVentasByNombreComprador(nombre).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /** Obtiene una venta por ID. */
    public VentaDto obtenerVentaPorId(String ventaId) {
        return ventaRepository.findById(Objects.requireNonNull(ventaId, "ventaId"))
                .map(this::convertirADto)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
    }

    /** Obtiene el total de ventas en un rango de fechas. */
    public BigDecimal obtenerTotalVentasEnRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findVentasByFechaRango(fechaInicio, fechaFin).stream()
                .map(Venta::getTotalVenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Obtiene las ventas asociadas a una orden de servicio. */
    public List<VentaDto> obtenerVentasPorOrden(String ordenId) {
        return ventaRepository.findByOrdenDeServicioId(ordenId).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    // ── Conversión entidad → DTO ────────────────────────────────────────────

    private VentaDto convertirADto(Venta venta) {
        List<VentaDetalleDto> detallesDto = venta.getDetalles() != null
            ? venta.getDetalles().stream()
                .map(d -> {
                    String tipoItem = d.getTipoItem() != null ? d.getTipoItem() : "PRODUCTO";
                    if (d.getProduct() != null) {
                        return new VentaDetalleDto(
                            d.getProduct().getId(),
                            d.getProduct().getName(),
                            "PRODUCTO",
                            d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
                    } else {
                        return new VentaDetalleDto(null, "(ítem desconocido)",
                            tipoItem,
                            d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
                    }
                })
                .collect(Collectors.toList())
            : java.util.Collections.emptyList();

        Cliente c = venta.getCliente();
        String nombreComprador   = c != null ? (c.getNombre() + " " + c.getApellido()).trim() : "";
        String telefonoComprador = c != null && c.getTelefono() != null ? c.getTelefono() : "";
        String emailComprador    = c != null && c.getEmail()    != null ? c.getEmail()    : "";

        VentaDto dto = new VentaDto(
            venta.getId(),
            venta.getTotalVenta(),
            nombreComprador,
            telefonoComprador,
            emailComprador,
            venta.getUsuario().getUsername(),
            (venta.getUsuario().getFirstName() != null ? venta.getUsuario().getFirstName() : "")
                + " " + (venta.getUsuario().getLastName() != null ? venta.getUsuario().getLastName() : ""),
            venta.getFecha(),
            venta.getObservaciones(),
            detallesDto
        );
        dto.setOrdenDeServicioId(venta.getOrdenDeServicioId());
        dto.setEstado(venta.getEstado());
        dto.setFormaPago(venta.getFormaPago());
        dto.setDescuento(venta.getDescuento());
        if (c != null) {
            dto.setClienteId(String.valueOf(c.getId()));
            dto.setClienteTipoDocumento(c.getTipoDocumentoId());
        }
        if (venta.getSede() != null) {
            dto.setCodigoSede(venta.getSede().getCodigoSede());
            dto.setNombreSede(venta.getSede().getNombre());
        }
        return dto;
    }
}
