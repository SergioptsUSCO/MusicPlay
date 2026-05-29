package com.musicplay.musicplay.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Artista;

@Repository
public interface ArtistaRepo extends JpaRepository<Artista,Long> {

    @Query("select a from Artista a where lower(a.artista_nombre) = lower(:nombre)")
    Optional<Artista> buscarPorNombre(@Param("nombre") String nombre);

    @Query("""
            select c.artista from HistorialReproduccion h
            join h.cancion c
            join c.artista a
            group by c.artista
            order by count(h.id) desc, max(h.fecha_reproduccion) desc
            """)
    List<Artista> artistasMasReproducidos(Pageable pageable);
}
