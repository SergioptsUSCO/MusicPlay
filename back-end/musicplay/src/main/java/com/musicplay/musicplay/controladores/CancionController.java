package com.musicplay.musicplay.controladores;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.repos.CancionRepo;

@RestController
public class CancionController {

    CancionRepo repositorio;

    //Constructor para el repositorio
    public CancionController(CancionRepo repositorio) {
        this.repositorio = repositorio;
    }

    //Método para crear una canción
    @SuppressWarnings("null")           //Ignora errores de tipo null
    @PostMapping("/api/crearCancion")   //Método Get
    public void crearCancion() {

        Cancion cancion1 = new Cancion("Despacito","Luis Fonsi, Daddy Yankee","Luis Fonsi, Erika Ender","Reggaeton");
        Cancion cancion2 = new Cancion("Adán y Eva","Paulo Londra","Paulo Londra, Ovy on the Drums","Reggaeton");
        Cancion cancion3 = new Cancion("Tití me preguntó","Bad Bunny","Benito Antonio Martínez Ocasio, Marco Daniel Borrero","Reggaeton");
        
        //Guardar las canciones en la base de datos
        List<Cancion> lista = List.of(cancion1,cancion2,cancion3);
        repositorio.saveAll(lista);

    }

    //Método Get para obtener los registros de aplicacion en la base de datos
    @GetMapping("/api/canciones")
    public List<Cancion> obtenerCancion() {
        return repositorio.findAll();
    }
}
