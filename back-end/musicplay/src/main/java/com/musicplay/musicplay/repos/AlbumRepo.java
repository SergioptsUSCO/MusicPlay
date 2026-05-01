package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Album;

@Repository
public interface AlbumRepo extends JpaRepository<Album,Long> {

}
