package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.CancionesPlaylist;

@Repository
public interface CancionesPlaylistRepo extends JpaRepository<CancionesPlaylist, Long> {

}
