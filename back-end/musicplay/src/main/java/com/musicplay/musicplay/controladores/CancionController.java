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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.musicplay.musicplay.modelos.Album;
import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.modelos.CancionesAlbum;
import com.musicplay.musicplay.repos.AlbumRepo;
import com.musicplay.musicplay.repos.ArtistaRepo;
import com.musicplay.musicplay.repos.CancionesAlbumRepo;
import com.musicplay.musicplay.repos.CancionRepo;
import com.musicplay.musicplay.services.ArchivoStorageService;

@RestController
public class CancionController {

    private final CancionRepo repositorio;
    private final AlbumRepo albumRepo;
    private final ArtistaRepo artistaRepo;
    private final CancionesAlbumRepo cancionesAlbumRepo;
    private final ArchivoStorageService storageService;

    public CancionController(
            CancionRepo repositorio,
            AlbumRepo albumRepo,
            ArtistaRepo artistaRepo,
            CancionesAlbumRepo cancionesAlbumRepo,
            ArchivoStorageService storageService) {
        this.repositorio = repositorio;
        this.albumRepo = albumRepo;
        this.artistaRepo = artistaRepo;
        this.cancionesAlbumRepo = cancionesAlbumRepo;
        this.storageService = storageService;
    }

    @SuppressWarnings("null")
    @CrossOrigin("*")
    @PostMapping(value = "/api/crearCancion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void crearCancion(@RequestBody Cancion cancion) {
        repositorio.save(cancion);
    }

    @CrossOrigin("*")
    @PostMapping(value = "/api/crearCancion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Cancion> crearCancionConArchivos(
            @RequestParam String song_nombre,
            @RequestParam String artista_nombre,
            @RequestParam(required = false) String song_compositor,
            @RequestParam Long song_genero,
            @RequestParam(required = false) String album_nombre,
            @RequestParam(required = false) MultipartFile archivoCancion,
            @RequestParam(required = false) MultipartFile portadaCancion) throws IOException {

        Artista artista = buscarArtista(artista_nombre);
        Album album = buscarAlbum(album_nombre, artista.getArtista_id());

        Cancion cancion = new Cancion();
        cancion.setSong_nombre(song_nombre);
        cancion.setSong_artista(artista.getArtista_id());
        cancion.setSong_compositor(song_compositor);
        cancion.setSong_genero(song_genero);
        asignarArchivos(cancion, archivoCancion, portadaCancion);

        Cancion cancionGuardada = repositorio.save(cancion);
        guardarRelacionAlbum(cancionGuardada.getSong_id(), album);

        return ResponseEntity.ok(cancionGuardada);
    }

    @CrossOrigin("*")
    @GetMapping("/api/buscarCancion/{song_id}")
    public ResponseEntity<Cancion> obtenerCancion(@NonNull @PathVariable Long song_id) {
        Optional<Cancion> opt = repositorio.findById(song_id);

        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }

        return ResponseEntity.notFound().build();
    }

    @CrossOrigin("*")
    @PutMapping(value = "/api/actualizarCancion/{song_id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> actualizarCancion(
            @PathVariable @NonNull Long song_id,
            @RequestBody Cancion datosActualizados) {

        Optional<Cancion> opt = repositorio.findById(song_id);

        if (opt.isPresent()) {
            Cancion cancion = opt.get();
            cancion.setSong_nombre(datosActualizados.getSong_nombre());
            cancion.setSong_artista(datosActualizados.getSong_artista());
            cancion.setSong_compositor(datosActualizados.getSong_compositor());
            cancion.setSong_genero(datosActualizados.getSong_genero());
            cancion.setSong_archivo_ruta(datosActualizados.getSong_archivo_ruta());
            cancion.setSong_portada_ruta(datosActualizados.getSong_portada_ruta());
            repositorio.save(cancion);
            return ResponseEntity.ok("Datos actualizados con exito");
        }

        return ResponseEntity.notFound().build();
    }

    @CrossOrigin("*")
    @PutMapping(value = "/api/actualizarCancion/{song_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Cancion> actualizarCancionConArchivos(
            @PathVariable @NonNull Long song_id,
            @RequestParam String song_nombre,
            @RequestParam String artista_nombre,
            @RequestParam(required = false) String song_compositor,
            @RequestParam Long song_genero,
            @RequestParam(required = false) String album_nombre,
            @RequestParam(required = false) MultipartFile archivoCancion,
            @RequestParam(required = false) MultipartFile portadaCancion) throws IOException {

        Optional<Cancion> opt = repositorio.findById(song_id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Artista artista = buscarArtista(artista_nombre);
        Album album = buscarAlbum(album_nombre, artista.getArtista_id());
        Cancion cancion = opt.get();
        cancion.setSong_nombre(song_nombre);
        cancion.setSong_artista(artista.getArtista_id());
        cancion.setSong_compositor(song_compositor);
        cancion.setSong_genero(song_genero);
        asignarArchivos(cancion, archivoCancion, portadaCancion);

        Cancion cancionGuardada = repositorio.save(cancion);
        actualizarRelacionAlbum(cancionGuardada.getSong_id(), album);

        return ResponseEntity.ok(cancionGuardada);
    }

    @SuppressWarnings("null")
    @CrossOrigin("*")
    @DeleteMapping("/api/eliminarCancion/{song_id}")
    public ResponseEntity<Cancion> eliminarCancion(@PathVariable @NonNull Long song_id) {
        Optional<Cancion> opt = repositorio.findById(song_id);

        if (opt.isPresent()) {
            repositorio.delete(opt.get());
            return ResponseEntity.ok(opt.get());
        }

        return ResponseEntity.notFound().build();
    }

    private void asignarArchivos(
            Cancion cancion,
            MultipartFile archivoCancion,
            MultipartFile portadaCancion) throws IOException {

        String rutaAudio = storageService.guardar(archivoCancion, "canciones");
        if (rutaAudio != null) {
            cancion.setSong_archivo_ruta(rutaAudio);
        }

        String rutaPortada = storageService.guardar(portadaCancion, "portadas-canciones");
        if (rutaPortada != null) {
            cancion.setSong_portada_ruta(rutaPortada);
        }
    }

    private Artista buscarArtista(String nombre) {
        return artistaRepo.buscarPorNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("El artista no existe: " + nombre));
    }

    private Album buscarAlbum(String nombre, Long artistaId) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        return albumRepo.buscarPorNombreYArtista(nombre, artistaId)
                .orElseThrow(() -> new IllegalArgumentException("El album no existe para ese artista: " + nombre));
    }

    private void guardarRelacionAlbum(Long cancionId, Album album) {
        if (album != null) {
            cancionesAlbumRepo.save(new CancionesAlbum(cancionId, album.getAlbum_id(), null, null));
        }
    }

    private void actualizarRelacionAlbum(Long cancionId, Album album) {
        cancionesAlbumRepo.deleteAll(cancionesAlbumRepo.buscarPorCancion(cancionId));
        guardarRelacionAlbum(cancionId, album);
    }

    @CrossOrigin("*")
    @GetMapping("/api/canciones")
    public ResponseEntity<List<Cancion>> listarCanciones() {
        return ResponseEntity.ok(repositorio.findAll());
    }

    @CrossOrigin("*")
    @GetMapping("/api/canciones/{song_id}/album")
    public ResponseEntity<Album> obtenerAlbumDeCancion(@PathVariable @NonNull Long song_id) {
        List<CancionesAlbum> relaciones = cancionesAlbumRepo.buscarPorCancion(song_id);

        if (relaciones.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return albumRepo.findById(relaciones.get(0).getAlbum_id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
