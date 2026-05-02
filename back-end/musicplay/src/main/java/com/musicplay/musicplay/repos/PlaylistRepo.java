package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Playlist;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist, Long> {

}
