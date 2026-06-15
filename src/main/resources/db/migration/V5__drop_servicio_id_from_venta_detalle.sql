ALTER TABLE venta_detalle
    DROP CONSTRAINT IF EXISTS fk_venta_detalle_servicio;

ALTER TABLE venta_detalle
    DROP COLUMN IF EXISTS servicio_id;
