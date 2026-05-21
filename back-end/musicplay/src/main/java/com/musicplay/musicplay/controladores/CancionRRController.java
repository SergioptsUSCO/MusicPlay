package com.musicplay.musicplay.controladores;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicplay.musicplay.dto.CancionRR;
import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.repos.CancionRepo;

@RestController
public class CancionRRController {

    private final CancionRepo repositorio;       //Seleccionando el repositorio

    //Constructor para el repositorio
    public CancionRRController(CancionRepo repositorio) {
        this.repositorio = repositorio;
    }

    //Método Get para la seccion de Canciones recientemente reproducidas
    @CrossOrigin("*")
    @GetMapping("/api/recientementeReproducidas")
    public ResponseEntity<List<CancionRR>> cancionesRecientes() {
        // Devolver las últimas 10 canciones por id (más recientes)
        List<Cancion> recent = repositorio.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "song_id"))).getContent();

        List<CancionRR> dto = recent.stream()
                .map(c -> new CancionRR(c.getSong_nombre(), String.valueOf(c.getSong_artista())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

}
