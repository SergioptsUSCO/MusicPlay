package com.musicplay.musicplay.repos;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.HistorialReproduccion;

@Repository
public interface HistorialReproduccionRepo extends JpaRepository<HistorialReproduccion, Long> {

    @Query("""
            select h from HistorialReproduccion h
            join fetch h.cancion c
            left join fetch c.artista
            order by h.fecha_reproduccion desc
            """)
    List<HistorialReproduccion> recientes(Pageable pageable);
}
