package com.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Línea de detalle de una venta.
 * La venta se compone únicamente de productos físicos; la asociación con una
 * orden de servicio vive en la cabecera de la venta.
 */
@Entity
@Table(name = "venta_detalle")
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    /** Referencia al producto físico vendido. */
    @ManyToOne(optional = true)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    /**
     * Tipo de ítem de la línea.
     * Se conserva para compatibilidad, pero actualmente solo se usa PRODUCTO.
     */
    @Column(name = "tipo_item", length = 20)
    private String tipoItem = "PRODUCTO";

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private BigDecimal precioUnitario;

    /** Descuento aplicado a esta línea (en moneda). Default 0. */
    @Column(name = "descuento", precision = 10, scale = 2, nullable = false)
    private BigDecimal descuento = BigDecimal.ZERO;

    /** Impuesto (IVA) aplicado a esta línea (en moneda). Default 0. */
    @Column(name = "impuesto", precision = 10, scale = 2, nullable = false)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal subtotal;

    public VentaDetalle() {}

    /** Constructor para líneas de PRODUCTO físico. */
    public VentaDetalle(Venta venta, Product product, Integer cantidad, BigDecimal precioUnitario) {
        this.venta = venta;
        this.product = product;
        this.tipoItem = "PRODUCTO";
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getDescuento() { return descuento != null ? descuento : BigDecimal.ZERO; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getImpuesto() { return impuesto != null ? impuesto : BigDecimal.ZERO; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}