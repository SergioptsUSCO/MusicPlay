package com.musicplay.musicplay.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.UsuarioOAuth;

@Repository
public interface UsuarioOAuthRepo extends JpaRepository<UsuarioOAuth, Long> {
    @Query("""
            SELECT uo FROM UsuarioOAuth uo
            JOIN FETCH uo.usuario
            WHERE uo.oauth_proveedor = :proveedor
            AND uo.oauth_proveedor_id = :proveedorId
            """)
    Optional<UsuarioOAuth> findByOauthProveedorAndOauthProveedorId(
            @Param("proveedor") String oauth_proveedor,
            @Param("proveedorId") String oauth_proveedor_id
    );
}
