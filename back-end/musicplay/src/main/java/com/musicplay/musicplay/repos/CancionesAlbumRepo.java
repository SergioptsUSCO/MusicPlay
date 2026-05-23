package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.CancionesAlbum;
import com.musicplay.musicplay.modelos.CancionesAlbumId;

@Repository
public interface CancionesAlbumRepo extends JpaRepository<CancionesAlbum,CancionesAlbumId>{

    @Query("select ca from CancionesAlbum ca where ca.cancion_id = :cancionId")
    List<CancionesAlbum> buscarPorCancion(@Param("cancionId") Long cancionId);
}
