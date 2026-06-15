package com.inventory.repository;

import com.inventory.model.Sede;
import com.inventory.model.User;
import com.inventory.model.UsuarioSede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioSedeRepository extends JpaRepository<UsuarioSede, Long> {

    List<UsuarioSede> findByUsuario(User usuario);

    List<UsuarioSede> findBySede(Sede sede);

    Optional<UsuarioSede> findByUsuarioAndSede(User usuario, Sede sede);

    @Query("SELECT us FROM UsuarioSede us WHERE UPPER(us.usuario.username) = UPPER(:username) AND us.activo = TRUE")
    List<UsuarioSede> findByUsuarioUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN TRUE ELSE FALSE END " +
           "FROM UsuarioSede us " +
           "WHERE UPPER(us.usuario.username) = UPPER(:username) " +
           "AND UPPER(us.sede.codigoSede) = UPPER(:codigoSede) " +
           "AND us.activo = TRUE AND us.sede.activo = TRUE")
    boolean existsByUsuarioUsernameAndCodigoSede(@Param("username") String username,
                                                  @Param("codigoSede") String codigoSede);

    @Query("SELECT us.sede FROM UsuarioSede us " +
           "WHERE UPPER(us.usuario.username) = UPPER(:username) " +
           "AND us.activo = TRUE AND us.sede.activo = TRUE")
    List<Sede> findSedesActivasByUsuarioUsername(@Param("username") String username);

    void deleteByUsuarioAndSede(User usuario, Sede sede);

    void deleteBySede(Sede sede);
}
