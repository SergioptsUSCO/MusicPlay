package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.modelos.UsuarioArtistaPreferido;
import com.musicplay.musicplay.modelos.UsuarioArtistaPreferidoId;

@Repository
public interface UsuarioArtistaPreferidoRepo extends JpaRepository<UsuarioArtistaPreferido, UsuarioArtistaPreferidoId> {

    @Query("select ua.artista from UsuarioArtistaPreferido ua where ua.usuario_id = :usuarioId order by ua.artista.artista_nombre")
    List<Artista> buscarArtistasPorUsuario(@Param("usuarioId") Long usuarioId);
}
