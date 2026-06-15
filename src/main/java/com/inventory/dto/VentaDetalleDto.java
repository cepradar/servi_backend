package com.inventory.dto;

import java.math.BigDecimal;

/**
 * DTO para representar una línea de detalle de venta en la respuesta API.
 */
public class VentaDetalleDto {

    // ── Campos producto (retro-compatibles) ──────────────────────────────────
    private String productId;
    private String productNombre;

    // ── Discriminador: PRODUCTO ──────────────────────────────────────────────
    private String tipoItem = "PRODUCTO";

    // ── Campos comunes ───────────────────────────────────────────────────────
    private Integer cantidad;
    private BigDecimal precioUnitario;
    /** Descuento aplicado a esta línea (en moneda). */
    private BigDecimal descuento;
    /** Impuesto (IVA) aplicado a esta línea (en moneda). */
    private BigDecimal impuesto;
    private BigDecimal subtotal;

    public VentaDetalleDto() {}

    /** Constructor retro-compatible para ítems de producto. */
    public VentaDetalleDto(String productId, String productNombre,
                           Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.productId = productId;
        this.productNombre = productNombre;
        this.tipoItem = "PRODUCTO";
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    /** Constructor completo con discriminador. */
    public VentaDetalleDto(String productId, String productNombre,
                           String tipoItem,
                           Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.productId = productId;
        this.productNombre = productNombre;
        this.tipoItem = tipoItem != null ? tipoItem : "PRODUCTO";
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductNombre() { return productNombre; }
    public void setProductNombre(String productNombre) { this.productNombre = productNombre; }

    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}