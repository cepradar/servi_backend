package com.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Detalle de una OrdenDeServicio.
 * Cada registro representa un servicio aplicado dentro de la orden.
 *
 * - PK: Long id (IDENTITY)
 * - UK: (orden_de_servicio_id, servicio_id, reg_servicio) para evitar duplicados
 * - regServicio: consecutivo dentro de cada orden (1, 2, 3...)
 * - Flags operacionales: activo, entregado, reparado, diagnosticado
 * - Fechas de trazabilidad: fechaEntregado, fechaReparado, fechaDiagnosticado, fechaModificacion
 */
@Entity
@Table(
    name = "orden_de_servicio_detalle",
    indexes = {
        @Index(name = "idx_osd_orden_id",    columnList = "orden_de_servicio_id"),
        @Index(name = "idx_osd_servicio_id", columnList = "servicio_id"),
        @Index(name = "idx_osd_activo",      columnList = "activo"),
        @Index(name = "idx_osd_reparado",    columnList = "reparado"),
        @Index(name = "idx_osd_entregado",   columnList = "entregado")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_osd_orden_servicio_reg",
            columnNames = {"orden_de_servicio_id", "servicio_id", "reg_servicio"}
        )
    }
)
public class OrdenDeServicioDetalle {

    // ── Clave primaria ────────────────────────────────────────────────────────

    @Id
    @Column(length = 25, nullable = false, unique = true)
    private String id;

    // ── Relaciones ────────────────────────────────────────────────────────────

    /** Orden de servicio a la que pertenece este detalle. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_de_servicio_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_osd_orden"))
    @JsonIgnoreProperties({"detalle", "usuario", "tecnicoAsignado", "cliente",
                            "clienteElectrodomestico", "sede"})
    private OrdenDeServicio ordenDeServicio;

    /** Servicio aplicado (mano de obra, diagnóstico, mantenimiento, etc.). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_osd_servicio"))
    private Servicio servicio;

    // ── Consecutivo dentro de la orden ───────────────────────────────────────

    /**
     * Número de línea dentro de la orden (1, 2, 3…).
     * Se asigna a nivel de aplicación antes de persistir.
     */
    @Column(name = "reg_servicio", nullable = false)
    private Integer regServicio = 1;

    // ── Datos económicos ─────────────────────────────────────────────────────

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "cierre_tecnico", columnDefinition = "TEXT")
    private String cierreTecnico;

    // ── Flags operacionales ───────────────────────────────────────────────────

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private boolean entregado = false;

    @Column(nullable = false)
    private boolean reparado = false;

    @Column(nullable = false)
    private boolean diagnosticado = false;

    // ── Fechas de trazabilidad ────────────────────────────────────────────────

    @Column(name = "fecha_entregado")
    private LocalDateTime fechaEntregado;

    @Column(name = "fecha_reparado")
    private LocalDateTime fechaReparado;

    @Column(name = "fecha_diagnosticado")
    private LocalDateTime fechaDiagnosticado;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // ── Auditoría ─────────────────────────────────────────────────────────────

    /** Username del usuario que registró este detalle. */
    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    // ── Constructores ─────────────────────────────────────────────────────────

    public OrdenDeServicioDetalle() {}

    public OrdenDeServicioDetalle(OrdenDeServicio ordenDeServicio, Servicio servicio,
                                  Integer regServicio, Integer cantidad, BigDecimal precioUnitario,
                                  String usuarioRegistro) {
        this.ordenDeServicio = ordenDeServicio;
        this.servicio = servicio;
        this.regServicio = regServicio;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.usuarioRegistro = usuarioRegistro;
    }


    // ── Getters y Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public OrdenDeServicio getOrdenDeServicio() { return ordenDeServicio; }
    public void setOrdenDeServicio(OrdenDeServicio ordenDeServicio) { this.ordenDeServicio = ordenDeServicio; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public Integer getRegServicio() { return regServicio; }
    public void setRegServicio(Integer regServicio) { this.regServicio = regServicio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getCierreTecnico() { return cierreTecnico; }
    public void setCierreTecnico(String cierreTecnico) { this.cierreTecnico = cierreTecnico; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }

    public boolean isReparado() { return reparado; }
    public void setReparado(boolean reparado) { this.reparado = reparado; }

    public boolean isDiagnosticado() { return diagnosticado; }
    public void setDiagnosticado(boolean diagnosticado) { this.diagnosticado = diagnosticado; }

    public LocalDateTime getFechaEntregado() { return fechaEntregado; }
    public void setFechaEntregado(LocalDateTime fechaEntregado) { this.fechaEntregado = fechaEntregado; }

    public LocalDateTime getFechaReparado() { return fechaReparado; }
    public void setFechaReparado(LocalDateTime fechaReparado) { this.fechaReparado = fechaReparado; }

    public LocalDateTime getFechaDiagnosticado() { return fechaDiagnosticado; }
    public void setFechaDiagnosticado(LocalDateTime fechaDiagnosticado) { this.fechaDiagnosticado = fechaDiagnosticado; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }

    // ── equals / hashCode ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenDeServicioDetalle that = (OrdenDeServicioDetalle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "OrdenDeServicioDetalle{id=" + id +
               ", regServicio=" + regServicio +
               ", cantidad=" + cantidad +
               ", activo=" + activo + '}';
    }
}