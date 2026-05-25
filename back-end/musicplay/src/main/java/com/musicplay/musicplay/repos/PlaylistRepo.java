package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Playlist;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist, Long> {

    @Query("select p from Playlist p where p.usuario_id = :usuarioId order by p.playlist_nombre")
    List<Playlist> buscarPorUsuario(@Param("usuarioId") Long usuarioId);
}
