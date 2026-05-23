package com.musicplay.musicplay.controladores;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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

import com.musicplay.musicplay.modelos.Album;
import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.repos.AlbumRepo;
import com.musicplay.musicplay.repos.ArtistaRepo;
import com.musicplay.musicplay.services.ArchivoStorageService;

@RestController
public class AlbumController {

    private final AlbumRepo albumRepo;
    private final ArtistaRepo artistaRepo;
    private final ArchivoStorageService storageService;

    public AlbumController(AlbumRepo albumRepo, ArtistaRepo artistaRepo, ArchivoStorageService storageService) {
        this.albumRepo = albumRepo;
        this.artistaRepo = artistaRepo;
        this.storageService = storageService;
    }

    @CrossOrigin("*")
    @GetMapping("/api/albumes")
    public ResponseEntity<List<Album>> listarAlbumes() {
        return ResponseEntity.ok(albumRepo.findAll());
    }

    @CrossOrigin("*")
    @GetMapping("/api/albumes/artista")
    public ResponseEntity<List<Album>> listarAlbumesPorArtista(@RequestParam String artista_nombre) {
        Artista artista = buscarArtista(artista_nombre);
        return ResponseEntity.ok(albumRepo.buscarPorArtista(artista.getArtista_id()));
    }

    @CrossOrigin("*")
    @GetMapping("/api/buscarAlbum/{album_id}")
    public ResponseEntity<Album> obtenerAlbum(@NonNull @PathVariable Long album_id) {
        return albumRepo.findById(album_id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @CrossOrigin("*")
    @PostMapping(value = "/api/crearAlbum", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Album> crearAlbum(
            @RequestParam String album_nombre,
            @RequestParam String artista_nombre,
            @RequestParam(required = false) MultipartFile portadaAlbum) throws IOException {

        Artista artista = buscarArtista(artista_nombre);
        Album album = new Album();
        album.setAlbum_nombre(album_nombre);
        album.setArtista_album(artista.getArtista_id());
        asignarPortada(album, portadaAlbum);
        return ResponseEntity.ok(albumRepo.save(album));
    }

    @CrossOrigin("*")
    @PutMapping(value = "/api/actualizarAlbum/{album_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Album> actualizarAlbum(
            @PathVariable @NonNull Long album_id,
            @RequestParam String album_nombre,
            @RequestParam String artista_nombre,
            @RequestParam(required = false) MultipartFile portadaAlbum) throws IOException {

        Optional<Album> opt = albumRepo.findById(album_id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Artista artista = buscarArtista(artista_nombre);
        Album album = opt.get();
        album.setAlbum_nombre(album_nombre);
        album.setArtista_album(artista.getArtista_id());
        asignarPortada(album, portadaAlbum);
        return ResponseEntity.ok(albumRepo.save(album));
    }

    @CrossOrigin("*")
    @DeleteMapping("/api/eliminarAlbum/{album_id}")
    public ResponseEntity<Album> eliminarAlbum(@PathVariable @NonNull Long album_id) {
        Optional<Album> opt = albumRepo.findById(album_id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        albumRepo.delete(opt.get());
        return ResponseEntity.ok(opt.get());
    }

    private Artista buscarArtista(String nombre) {
        return artistaRepo.buscarPorNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("El artista no existe: " + nombre));
    }

    private void asignarPortada(Album album, MultipartFile portadaAlbum) throws IOException {
        String rutaPortada = storageService.guardar(portadaAlbum, "portadas-albumes");
        if (rutaPortada != null) {
            album.setAlbum_portada_ruta(rutaPortada);
        }
    }
}
