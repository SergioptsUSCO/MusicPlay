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
import com.musicplay.musicplay.modelos.HistorialReproduccion;
import com.musicplay.musicplay.repos.CancionRepo;
import com.musicplay.musicplay.repos.HistorialReproduccionRepo;

@RestController
public class CancionRRController {

    private final CancionRepo repositorio;
    private final HistorialReproduccionRepo historialRepo;

    public CancionRRController(CancionRepo repositorio, HistorialReproduccionRepo historialRepo) {
        this.repositorio = repositorio;
        this.historialRepo = historialRepo;
    }

    @CrossOrigin("*")
    @GetMapping("/api/recientementeReproducidas")
    public ResponseEntity<List<CancionRR>> cancionesRecientes() {
        List<HistorialReproduccion> historial = historialRepo.recientes(PageRequest.of(0, 10));

        if (!historial.isEmpty()) {
            List<CancionRR> dto = historial.stream()
                    .map(h -> toDto(h.getCancion()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dto);
        }

        List<Cancion> recent = repositorio
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "song_id")))
                .getContent();

        List<CancionRR> dto = recent.stream()
                .map(c -> new CancionRR(
                        c.getSong_nombre(),
                        String.valueOf(c.getSong_artista()),
                        c.getSong_portada_ruta()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    private CancionRR toDto(Cancion cancion) {
        String artista = cancion.getArtista() != null
                ? cancion.getArtista().getArtista_nombre()
                : String.valueOf(cancion.getSong_artista());

        return new CancionRR(
                cancion.getSong_nombre(),
                artista,
                cancion.getSong_portada_ruta()
        );
    }
}
