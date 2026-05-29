package com.musicplay.musicplay.controladores;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.repos.ArtistaRepo;
import com.musicplay.musicplay.services.ArchivoStorageService;

@RestController
public class ArtistaController {

    private final ArtistaRepo artistaRepo;
    private final ArchivoStorageService storageService;

    public ArtistaController(ArtistaRepo artistaRepo, ArchivoStorageService storageService) {
        this.artistaRepo = artistaRepo;
        this.storageService = storageService;
    }

    @CrossOrigin("*")
    @GetMapping("/api/artistas")
    public ResponseEntity<List<Artista>> listarArtistas() {
        return ResponseEntity.ok(artistaRepo.findAll());
    }

    @CrossOrigin("*")
    @GetMapping("/api/tendencias/artistas")
    public ResponseEntity<List<Artista>> listarArtistasEnTendencia() {
        List<Artista> artistas = artistaRepo.artistasMasReproducidos(PageRequest.of(0, 6));

        if (artistas.isEmpty()) {
            artistas = artistaRepo.findAll(PageRequest.of(0, 6)).getContent();
        }

        return ResponseEntity.ok(artistas);
    }

    @CrossOrigin("*")
    @GetMapping("/api/buscarArtista/{artista_id}")
    public ResponseEntity<Artista> obtenerArtista(@NonNull @PathVariable Long artista_id) {
        return artistaRepo.findById(artista_id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @CrossOrigin("*")
    @PostMapping(value = "/api/crearArtista", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Artista> crearArtista(
            @RequestParam String artista_nombre,
            @RequestParam(required = false) MultipartFile fotoArtista) throws IOException {

        Artista artista = new Artista();
        artista.setArtista_nombre(artista_nombre);
        asignarFoto(artista, fotoArtista);
        return ResponseEntity.ok(artistaRepo.save(artista));
    }

    @CrossOrigin("*")
    @PutMapping(value = "/api/actualizarArtista/{artista_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Artista> actualizarArtista(
            @PathVariable @NonNull Long artista_id,
            @RequestParam String artista_nombre,
            @RequestParam(required = false) MultipartFile fotoArtista) throws IOException {

        Optional<Artista> opt = artistaRepo.findById(artista_id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Artista artista = opt.get();
        artista.setArtista_nombre(artista_nombre);
        asignarFoto(artista, fotoArtista);
        return ResponseEntity.ok(artistaRepo.save(artista));
    }

    @CrossOrigin("*")
    @DeleteMapping("/api/eliminarArtista/{artista_id}")
    public ResponseEntity<Artista> eliminarArtista(@PathVariable @NonNull Long artista_id) {
        Optional<Artista> opt = artistaRepo.findById(artista_id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        artistaRepo.delete(opt.get());
        return ResponseEntity.ok(opt.get());
    }

    private void asignarFoto(Artista artista, MultipartFile fotoArtista) throws IOException {
        String rutaFoto = storageService.guardar(fotoArtista, "fotos-artistas");
        if (rutaFoto != null) {
            artista.setArtista_foto_ruta(rutaFoto);
        }
    }
}
