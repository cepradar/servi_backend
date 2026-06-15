package com.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenDeServicioDto {
    private String id;
    /** Código de la sede donde se registró la orden. Ej: BQ, MDE, BOG */
    private String codigoSede;
    /** Nombre de la sede para mostrar en UI. */
    private String nombreSede;
    private String clienteId;
    private String clienteTipoDocumentoId;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteTelefono;
    private String clienteEmail;
    private Long electrodomesticoId;
    private String electrodomesticoTipo;
    private String electrodomesticoMarca;
    private String electrodomesticoModelo;
    private String tipoServicio;
    private String descripcionProblema;
    private String diagnostico;
    private String solucion;
    private String partesCambiadas;
    private BigDecimal costoServicio;
    private BigDecimal costoRepuestos;
    private BigDecimal totalCosto;
    private String estado;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private Integer garantiaServicio;
    private LocalDate vencimientoGarantia;
    private String usuarioUsername;
    private String usuarioNombre;
    private String tecnicoAsignadoUsername;
    private String tecnicoAsignadoNombre;
    private LocalDateTime fechaAsignacion;
    private String codigoEstado;
    private String observaciones;
    private boolean activo;
    private LocalDateTime fechaReparado;
    private LocalDateTime fechaEntrega;
    private boolean entregado;
    private List<OrdenDeServicioDetalleDto> detalles = new ArrayList<>();

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigoSede() { return codigoSede; }
    public void setCodigoSede(String codigoSede) { this.codigoSede = codigoSede; }

    public String getNombreSede() { return nombreSede; }
    public void setNombreSede(String nombreSede) { this.nombreSede = nombreSede; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getClienteTipoDocumentoId() { return clienteTipoDocumentoId; }
    public void setClienteTipoDocumentoId(String clienteTipoDocumentoId) { this.clienteTipoDocumentoId = clienteTipoDocumentoId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteApellido() { return clienteApellido; }
    public void setClienteApellido(String clienteApellido) { this.clienteApellido = clienteApellido; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public Long getElectrodomesticoId() { return electrodomesticoId; }
    public void setElectrodomesticoId(Long electrodomesticoId) { this.electrodomesticoId = electrodomesticoId; }

    public String getElectrodomesticoTipo() { return electrodomesticoTipo; }
    public void setElectrodomesticoTipo(String electrodomesticoTipo) { this.electrodomesticoTipo = electrodomesticoTipo; }

    public String getElectrodomesticoMarca() { return electrodomesticoMarca; }
    public void setElectrodomesticoMarca(String electrodomesticoMarca) { this.electrodomesticoMarca = electrodomesticoMarca; }

    public String getElectrodomesticoModelo() { return electrodomesticoModelo; }
    public void setElectrodomesticoModelo(String electrodomesticoModelo) { this.electrodomesticoModelo = electrodomesticoModelo; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getDescripcionProblema() { return descripcionProblema; }
    public void setDescripcionProblema(String descripcionProblema) { this.descripcionProblema = descripcionProblema; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getSolucion() { return solucion; }
    public void setSolucion(String solucion) { this.solucion = solucion; }

    public String getPartesCambiadas() { return partesCambiadas; }
    public void setPartesCambiadas(String partesCambiadas) { this.partesCambiadas = partesCambiadas; }

    public BigDecimal getCostoServicio() { return costoServicio; }
    public void setCostoServicio(BigDecimal costoServicio) { this.costoServicio = costoServicio; }

    public BigDecimal getCostoRepuestos() { return costoRepuestos; }
    public void setCostoRepuestos(BigDecimal costoRepuestos) { this.costoRepuestos = costoRepuestos; }

    public BigDecimal getTotalCosto() { return totalCosto; }
    public void setTotalCosto(BigDecimal totalCosto) { this.totalCosto = totalCosto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public Integer getGarantiaServicio() { return garantiaServicio; }
    public void setGarantiaServicio(Integer garantiaServicio) { this.garantiaServicio = garantiaServicio; }

    public LocalDate getVencimientoGarantia() { return vencimientoGarantia; }
    public void setVencimientoGarantia(LocalDate vencimientoGarantia) { this.vencimientoGarantia = vencimientoGarantia; }

    public String getUsuarioUsername() { return usuarioUsername; }
    public void setUsuarioUsername(String usuarioUsername) { this.usuarioUsername = usuarioUsername; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getTecnicoAsignadoUsername() { return tecnicoAsignadoUsername; }
    public void setTecnicoAsignadoUsername(String tecnicoAsignadoUsername) { this.tecnicoAsignadoUsername = tecnicoAsignadoUsername; }

    public String getTecnicoAsignadoNombre() { return tecnicoAsignadoNombre; }
    public void setTecnicoAsignadoNombre(String tecnicoAsignadoNombre) { this.tecnicoAsignadoNombre = tecnicoAsignadoNombre; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public String getCodigoEstado() { return codigoEstado; }
    public void setCodigoEstado(String codigoEstado) { this.codigoEstado = codigoEstado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaReparado() { return fechaReparado; }
    public void setFechaReparado(LocalDateTime fechaReparado) { this.fechaReparado = fechaReparado; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }

    public List<OrdenDeServicioDetalleDto> getDetalles() { return detalles; }
    public void setDetalles(List<OrdenDeServicioDetalleDto> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }
}
