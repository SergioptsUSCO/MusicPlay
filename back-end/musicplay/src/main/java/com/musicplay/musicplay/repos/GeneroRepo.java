package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Genero;

@Repository
public interface GeneroRepo extends JpaRepository<Genero, Long> {

}
