package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.CancionesAlbum;
import com.musicplay.musicplay.modelos.CancionesAlbumId;
import com.musicplay.musicplay.modelos.Cancion;

@Repository
public interface CancionesAlbumRepo extends JpaRepository<CancionesAlbum,CancionesAlbumId>{

    @Query("select ca from CancionesAlbum ca where ca.cancion_id = :cancionId")
    List<CancionesAlbum> buscarPorCancion(@Param("cancionId") Long cancionId);

    @Query("select ca from CancionesAlbum ca where ca.album_id = :albumId")
    List<CancionesAlbum> buscarPorAlbum(@Param("albumId") Long albumId);

    @Query("select ca.cancion from CancionesAlbum ca where ca.album_id = :albumId order by ca.cancion.song_nombre")
    List<Cancion> buscarCancionesPorAlbum(@Param("albumId") Long albumId);
}
