package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.musicplay.musicplay.modelos.Genero;

@Repository
public interface GeneroRepo extends JpaRepository<Genero, Long> {

    @Query("select g from Genero g where lower(g.nombre_genero) = lower(:nombre)")
    Optional<Genero> buscarPorNombre(@Param("nombre") String nombre);
}
