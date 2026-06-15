package com.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "orden_de_servicio")
public class OrdenDeServicio {
    
    @Id
    @Column(length = 25, nullable = false, unique = true)
    private String id;

    /**
     * Sede donde se registró la orden de servicio. FK a la tabla sedes.
     * El ID de la orden se genera con formato: O-{CODIGO_SEDE}-{CONSECUTIVO_6_DIGITOS}
     * Ejemplo: O-BQ-000198
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_sede", referencedColumnName = "codigo_sede",
                foreignKey = @ForeignKey(name = "fk_orden_servicio_sede"))
    private Sede sede;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
                foreignKey = @ForeignKey(name = "fk_orden_cliente"))
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "cliente_electrodomestico_id", nullable = false)
    private ClienteElectrodomestico clienteElectrodomestico;
    
    // REFACTOR: Productos ahora se manejan en el módulo de Ventas
    // Una orden de servicio puede tener múltiples ventas asociadas
    // No guardar relación directa con productos aquí
    
    @Column(nullable = false)
    private String tipoServicio; // REPARACION, MANTENIMIENTO, DIAGNOSTICO
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcionProblema;
    
    @Column(columnDefinition = "TEXT")
    private String diagnostico;
    
    @Column(columnDefinition = "TEXT")
    private String solucion;
    
    @Column(columnDefinition = "TEXT", name = "partes_cambiadas")
    private String partesCambiadas;
    
    @Column(precision = 10, scale = 2, name = "costo_servicio")
    private BigDecimal costoServicio = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2, name = "costo_repuestos")
    private BigDecimal costoRepuestos = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2, name = "total_costo")
    private BigDecimal totalCosto = BigDecimal.ZERO;
    
    /** Estado detallado de la orden (SOC, SOA, SOE, etc.). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado", referencedColumnName = "id", nullable = false)
    private Evento estado;
    
    @Column(name = "fecha_ingreso", nullable = false, updatable = false)
    private LocalDateTime fechaIngreso;
    
    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;
    
    @Column(name = "garantia_servicio")
    private Integer garantiaServicio = 30; // Días de garantía en la reparación (default 30)
    
    @Column(name = "vencimiento_garantia")
    private LocalDate vencimientoGarantia;
    
    @ManyToOne
    @JoinColumn(name = "usuario_username", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_asignado_username")
    private User tecnicoAsignado; // Técnico asignado para realizar el servicio

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Prioridad de atención: ALTA, MEDIA (default), BAJA.
     */
    @Column(length = 10, nullable = false)
    private String prioridad = "MEDIA";

    /**
     * Fecha y hora prometida de entrega al cliente.
     */
    @Column(name = "fecha_promesa_entrega", nullable = true)
    private LocalDateTime fechaPromesaEntrega;

    @Column(name = "fecha_reparado")
    private LocalDateTime fechaReparado;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(nullable = false)
    private boolean entregado = false;

    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Detalle de servicios aplicados en esta orden.
     * Cascada ALL + orphanRemoval para que los detalles se eliminen con la orden.
     */
    @OneToMany(mappedBy = "ordenDeServicio", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties("ordenDeServicio")
    private List<OrdenDeServicioDetalle> detalle = new ArrayList<>();
    
    // Constructores
    public OrdenDeServicio() {
        this.fechaIngreso = LocalDateTime.now();
        this.garantiaServicio = 30;
        this.costoServicio = BigDecimal.ZERO;
        this.costoRepuestos = BigDecimal.ZERO;
        this.totalCosto = BigDecimal.ZERO;
    }
    
    public OrdenDeServicio(Cliente cliente, ClienteElectrodomestico clienteElectrodomestico, 
                              String tipoServicio, String descripcionProblema, User usuario) {
        this();
        this.cliente = cliente;
        this.clienteElectrodomestico = clienteElectrodomestico;
        this.tipoServicio = tipoServicio;
        this.descripcionProblema = descripcionProblema;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public ClienteElectrodomestico getClienteElectrodomestico() {
        return clienteElectrodomestico;
    }
    
    public void setClienteElectrodomestico(ClienteElectrodomestico clienteElectrodomestico) {
        this.clienteElectrodomestico = clienteElectrodomestico;
    }
    
    public String getTipoServicio() {
        return tipoServicio;
    }
    
    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }
    
    public String getDescripcionProblema() {
        return descripcionProblema;
    }
    
    public void setDescripcionProblema(String descripcionProblema) {
        this.descripcionProblema = descripcionProblema;
    }
    
    public String getDiagnostico() {
        return diagnostico;
    }
    
    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }
    
    public String getSolucion() {
        return solucion;
    }
    
    public void setSolucion(String solucion) {
        this.solucion = solucion;
    }
    
    public String getPartesCambiadas() {
        return partesCambiadas;
    }
    
    public void setPartesCambiadas(String partesCambiadas) {
        this.partesCambiadas = partesCambiadas;
    }
    
    public BigDecimal getCostoServicio() {
        return costoServicio;
    }
    
    public void setCostoServicio(BigDecimal costoServicio) {
        this.costoServicio = costoServicio;
        recalcularTotal();
    }
    
    public BigDecimal getCostoRepuestos() {
        return costoRepuestos;
    }
    
    public void setCostoRepuestos(BigDecimal costoRepuestos) {
        this.costoRepuestos = costoRepuestos;
        recalcularTotal();
    }
    
    public BigDecimal getTotalCosto() {
        return totalCosto;
    }
    
    public void setTotalCosto(BigDecimal totalCosto) {
        this.totalCosto = totalCosto;
    }
    
    private void recalcularTotal() {
        this.totalCosto = (this.costoServicio != null ? this.costoServicio : BigDecimal.ZERO)
                .add(this.costoRepuestos != null ? this.costoRepuestos : BigDecimal.ZERO);
    }
    
    public Evento getEstado() {
        return estado;
    }
    
    public void setEstado(Evento estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }
    
    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
    
    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }
    
    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }
    
    public Integer getGarantiaServicio() {
        return garantiaServicio;
    }
    
    public void setGarantiaServicio(Integer garantiaServicio) {
        this.garantiaServicio = garantiaServicio;
    }
    
    public LocalDate getVencimientoGarantia() {
        return vencimientoGarantia;
    }
    
    public void setVencimientoGarantia(LocalDate vencimientoGarantia) {
        this.vencimientoGarantia = vencimientoGarantia;
    }
    
    public User getUsuario() {
        return usuario;
    }
    
    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public User getTecnicoAsignado() {
        return tecnicoAsignado;
    }

    public void setTecnicoAsignado(User tecnicoAsignado) {
        this.tecnicoAsignado = tecnicoAsignado;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public LocalDateTime getFechaPromesaEntrega() { return fechaPromesaEntrega; }
    public void setFechaPromesaEntrega(LocalDateTime fechaPromesaEntrega) { this.fechaPromesaEntrega = fechaPromesaEntrega; }

    public LocalDateTime getFechaReparado() { return fechaReparado; }
    public void setFechaReparado(LocalDateTime fechaReparado) { this.fechaReparado = fechaReparado; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<OrdenDeServicioDetalle> getDetalle() { return detalle; }
    public void setDetalle(List<OrdenDeServicioDetalle> detalle) { this.detalle = detalle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenDeServicio that = (OrdenDeServicio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
    
    @Override
    public String toString() {
        return "OrdenDeServicio{" +
                "id=" + id +
                ", sede=" + (sede != null ? sede.getCodigoSede() : "null") +
                ", tipoServicio='" + tipoServicio + '\'' +
                ", estado='" + estado + '\'' +
                ", totalCosto=" + totalCosto +
                ", fechaIngreso=" + fechaIngreso +
                ", garantiaServicio=" + garantiaServicio +
                '}';
    }
}
