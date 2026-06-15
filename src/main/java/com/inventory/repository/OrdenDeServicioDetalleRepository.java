package com.inventory.repository;

import com.inventory.model.OrdenDeServicioDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenDeServicioDetalleRepository extends JpaRepository<OrdenDeServicioDetalle, String> {
    /** Todos los detalles de una orden, ordenados por reg_servicio. */
    List<OrdenDeServicioDetalle> findByOrdenDeServicioIdOrderByRegServicio(String ordenDeServicioId);

    /** Detalles activos de una orden. */
    List<OrdenDeServicioDetalle> findByOrdenDeServicioIdAndActivoTrue(String ordenDeServicioId);

    /** Número de detalles en una orden (para calcular el próximo reg_servicio). */
    long countByOrdenDeServicioId(String ordenDeServicioId);

    /** Siguiente reg_servicio para una orden. */
    @Query("SELECT COALESCE(MAX(d.regServicio), 0) + 1 FROM OrdenDeServicioDetalle d WHERE d.ordenDeServicio.id = :ordenId")
    Integer nextRegServicio(@Param("ordenId") String ordenId);

    /** Buscar un detalle específico por orden y reg_servicio. */
    Optional<OrdenDeServicioDetalle> findByOrdenDeServicioIdAndRegServicio(String ordenDeServicioId, Integer regServicio);

    /** Detalles pendientes de diagnóstico en una orden. */
    List<OrdenDeServicioDetalle> findByOrdenDeServicioIdAndDiagnosticadoFalseAndActivoTrue(String ordenDeServicioId);

    /** Detalles reparados pero no entregados. */
    @Query("SELECT d FROM OrdenDeServicioDetalle d WHERE d.ordenDeServicio.id = :ordenId AND d.reparado = true AND d.entregado = false AND d.activo = true")
    List<OrdenDeServicioDetalle> findReparadosNoEntregados(@Param("ordenId") String ordenId);
}