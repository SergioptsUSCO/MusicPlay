package com.musicplay.musicplay.controladores;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @SuppressWarnings("null")          //Ignora errores de tipo null
    @CrossOrigin("*")
    @PostMapping("/api/crearCancion")   //Método POST
    public void crearCancion(@RequestBody Cancion cancion) {

        repositorio.save(cancion);

    }

    //Método Get para obtener los registros de aplicacion en la base de datos
    @CrossOrigin("*")
    @GetMapping("/api/buscarCancion/{song_id}")
    public ResponseEntity<Cancion> obtenerCancion(@NonNull @PathVariable Long song_id) {
        
        Optional<Cancion> opt = repositorio.findById(song_id);
        
        if (opt.isPresent()) {

            return ResponseEntity.ok(opt.get());

        } else {

            return ResponseEntity.notFound().build();

        }
        
    }


    //Metodo PUT para actualizar la informacion de una canción
    @CrossOrigin("*")
    @PutMapping("/api/actualizarCancion/{song_id}")
    public ResponseEntity<String> actualizarCancion(@PathVariable @NonNull Long song_id, @RequestBody Cancion datosActualizados) {
        
        Optional<Cancion> opt = repositorio.findById(song_id);
        
        if (opt.isPresent()) {
            
            Cancion cancion = opt.get();
            cancion.setSong_nombre(datosActualizados.getSong_nombre());
            cancion.setSong_artista(datosActualizados.getSong_artista());
            cancion.setSong_compositor(datosActualizados.getSong_compositor());
            cancion.setSong_genero(datosActualizados.getSong_genero());
            repositorio.save(cancion);
            return ResponseEntity.ok("Datos actualizados con éxito!!");

        } else {

            return ResponseEntity.notFound().build();

        }

    }

    @CrossOrigin("*")
    @DeleteMapping("/api/eliminarCancion/{song_id}")
    public ResponseEntity<Cancion> actualizarCancion(@PathVariable @NonNull Long song_id) {

        Optional<Cancion> opt = repositorio.findById(song_id);
        
        if (opt.isPresent()) {

            return ResponseEntity.ok(opt.get());

        } else {

            return ResponseEntity.notFound().build();

        }

    }



}
