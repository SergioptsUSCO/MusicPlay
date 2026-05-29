package com.musicplay.musicplay.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Usuario;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u WHERE u.usuario_correo = :correo")
    Optional<Usuario> findByUsuarioCorreo(@Param("correo") String usuario_correo);

    @Query("SELECT u FROM Usuario u WHERE u.usuario_nombre = :nombre")
    Optional<Usuario> findByUsuarioNombre(@Param("nombre") String usuario_nombre);

    @Modifying
    @Query("UPDATE Usuario u SET u.usuario_inicios_sesion = COALESCE(u.usuario_inicios_sesion, 0) + 1 WHERE u.usuario_correo = :correo")
    int incrementarIniciosSesionPorCorreo(@Param("correo") String usuario_correo);
}
