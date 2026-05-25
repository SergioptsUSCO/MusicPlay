package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.modelos.CancionesPlaylist;
import com.musicplay.musicplay.modelos.CancionesPlaylistId;

@Repository
public interface CancionesPlaylistRepo extends JpaRepository<CancionesPlaylist, CancionesPlaylistId> {

    @Query("select cp.cancion from CancionesPlaylist cp where cp.playlist_id = :playlistId order by cp.cancion.song_nombre")
    List<Cancion> buscarCancionesPorPlaylist(@Param("playlistId") Long playlistId);

    @Query("select cp from CancionesPlaylist cp where cp.playlist_id = :playlistId")
    List<CancionesPlaylist> buscarPorPlaylist(@Param("playlistId") Long playlistId);
}
