package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Genero;
import com.musicplay.musicplay.modelos.UsuarioGeneroPreferido;
import com.musicplay.musicplay.modelos.UsuarioGeneroPreferidoId;

@Repository
public interface UsuarioGeneroPreferidoRepo extends JpaRepository<UsuarioGeneroPreferido, UsuarioGeneroPreferidoId> {

    @Query("select ug.genero from UsuarioGeneroPreferido ug where ug.usuario_id = :usuarioId order by ug.genero.nombre_genero")
    List<Genero> buscarGenerosPorUsuario(@Param("usuarioId") Long usuarioId);
}
