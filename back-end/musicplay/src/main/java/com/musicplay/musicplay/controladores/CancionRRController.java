package com.musicplay.musicplay.controladores;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.repos.CancionRepo;

public class CancionRRController {

    CancionRepo repositorio;       //Seleccionando el repositorio

    //Constructor para el repositorio
    public CancionRRController(CancionRepo repositorio) {
        this.repositorio = repositorio;
    }

    
    //Método Get para la seccion de Canciones recientemente reproducidas
    @CrossOrigin("*")
    @GetMapping("/api/recientementeReproducidas")
    public void cancionesRecientes(@RequestBody Cancion cancion){



    }

}
