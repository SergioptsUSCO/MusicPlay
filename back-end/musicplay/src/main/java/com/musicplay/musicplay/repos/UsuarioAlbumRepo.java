package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Album;
import com.musicplay.musicplay.modelos.UsuarioAlbum;
import com.musicplay.musicplay.modelos.UsuarioAlbumId;

@Repository
public interface UsuarioAlbumRepo extends JpaRepository<UsuarioAlbum, UsuarioAlbumId> {

    @Query("select ua.album from UsuarioAlbum ua where ua.usuario_id = :usuarioId order by ua.album.album_nombre")
    List<Album> buscarAlbumesPorUsuario(@Param("usuarioId") Long usuarioId);
}
