package com.inventory.dto;

import java.math.BigDecimal;

/**
 * DTO para registrar una línea de venta desde el cliente.
 * Cada línea corresponde a un producto físico.
 */
public class VentaDetalleRegistroDto {

    /** ID del producto físico. */
    private String productId;

    /** Discriminador: PRODUCTO (default) | SERVICIO */
    private String tipoItem = "PRODUCTO";

    private Integer cantidad;
    private BigDecimal precioUnitario;
    /** Descuento en moneda para esta línea. Default 0 si no se envía. */
    private BigDecimal descuento = BigDecimal.ZERO;

    public VentaDetalleRegistroDto() {}

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getTipoItem() { return tipoItem != null ? tipoItem : "PRODUCTO"; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getDescuento() { return descuento != null ? descuento : BigDecimal.ZERO; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
}
