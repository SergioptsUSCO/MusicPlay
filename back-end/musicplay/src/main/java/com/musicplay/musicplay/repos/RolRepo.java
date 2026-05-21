package com.musicplay.musicplay.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Rol;

@Repository
public interface RolRepo extends JpaRepository<Rol, Long> {
    @Query("SELECT r FROM Rol r WHERE r.rol_nombre = :nombre")
    Optional<Rol> findByRolNombre(@Param("nombre") String rol_nombre);
}
