package com.musicplay.musicplay.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Album;

@Repository
public interface AlbumRepo extends JpaRepository<Album,Long> {

    @Query("select a from Album a where a.artista_album = :artistaId order by a.album_nombre")
    List<Album> buscarPorArtista(@Param("artistaId") Long artistaId);

    @Query("select a from Album a where lower(a.album_nombre) = lower(:nombre) and a.artista_album = :artistaId")
    Optional<Album> buscarPorNombreYArtista(@Param("nombre") String nombre, @Param("artistaId") Long artistaId);
}
