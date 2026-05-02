package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Rol;

@Repository
public interface RolRepo extends JpaRepository<Rol, Long> {

}
