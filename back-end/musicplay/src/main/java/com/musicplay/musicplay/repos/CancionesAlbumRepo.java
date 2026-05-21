package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.CancionesAlbum;
import com.musicplay.musicplay.modelos.CancionesAlbumId;

@Repository
public interface CancionesAlbumRepo extends JpaRepository<CancionesAlbum,CancionesAlbumId>{

}
