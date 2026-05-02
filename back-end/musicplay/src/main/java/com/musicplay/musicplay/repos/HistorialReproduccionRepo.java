package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.HistorialReproduccion;

@Repository
public interface HistorialReproduccionRepo extends JpaRepository<HistorialReproduccion, Long> {

}
