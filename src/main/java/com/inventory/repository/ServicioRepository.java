package com.inventory.repository;

import com.inventory.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    boolean existsByCodigo(String codigo);
    Optional<Servicio> findByCodigo(String codigo);
    Optional<Servicio> findByNombreIgnoreCaseAndActivoTrue(String nombre);
    List<Servicio> findByActivoTrue();
    List<Servicio> findByCategoriaServicio(String categoriaServicio);
    List<Servicio> findByActivoTrueOrderByNombreAsc();
}
