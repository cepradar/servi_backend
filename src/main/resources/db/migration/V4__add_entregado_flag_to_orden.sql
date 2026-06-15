ALTER TABLE orden_de_servicio
    ADD COLUMN IF NOT EXISTS entregado BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE orden_de_servicio
SET entregado = TRUE
WHERE entregado = FALSE
  AND (fecha_entrega IS NOT NULL OR CAST(estado AS TEXT) = 'SOENT');
