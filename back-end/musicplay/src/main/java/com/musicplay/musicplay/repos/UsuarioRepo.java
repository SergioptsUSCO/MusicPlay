package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Usuario;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {

}