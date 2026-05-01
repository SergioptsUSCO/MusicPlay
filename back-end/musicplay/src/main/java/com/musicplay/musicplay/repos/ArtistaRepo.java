package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Artista;

@Repository
public interface ArtistaRepo extends JpaRepository<Artista,Long> {

}
