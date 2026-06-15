package com.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdenDeServicioDetalleDto {

    private String id;
    private String ordenDeServicioId;
    private Integer regServicio;

    // Servicio
    private Long servicioId;
    private String servicioCodigo;
    private String servicioNombre;

    // Económicos
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String observaciones;
    private String cierreTecnico;

    // Flags operacionales
    private boolean activo;
    private boolean entregado;
    private boolean reparado;
    private boolean diagnosticado;

    // Trazabilidad
    private LocalDateTime fechaEntregado;
    private LocalDateTime fechaReparado;
    private LocalDateTime fechaDiagnosticado;
    private LocalDateTime fechaModificacion;
    private String usuarioRegistro;

    public OrdenDeServicioDetalleDto() {}

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrdenDeServicioId() { return ordenDeServicioId; }
    public void setOrdenDeServicioId(String ordenDeServicioId) { this.ordenDeServicioId = ordenDeServicioId; }

    public Integer getRegServicio() { return regServicio; }
    public void setRegServicio(Integer regServicio) { this.regServicio = regServicio; }

    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }

    public String getServicioCodigo() { return servicioCodigo; }
    public void setServicioCodigo(String servicioCodigo) { this.servicioCodigo = servicioCodigo; }

    public String getServicioNombre() { return servicioNombre; }
    public void setServicioNombre(String servicioNombre) { this.servicioNombre = servicioNombre; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

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
}