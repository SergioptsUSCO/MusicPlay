package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.CancionesAlbum;

@Repository
public interface CancionesAlbumRepo extends JpaRepository<CancionesAlbum,Long>{

}
