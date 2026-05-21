package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.UsuarioPlaylist;
import com.musicplay.musicplay.modelos.UsuarioPlaylistId;

@Repository
public interface UsuarioPlaylistRepo extends JpaRepository<UsuarioPlaylist, UsuarioPlaylistId> {

}
