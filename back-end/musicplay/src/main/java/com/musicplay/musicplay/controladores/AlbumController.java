package com.musicplay.musicplay.controladores;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
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
import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.modelos.CancionesAlbumId;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.modelos.UsuarioAlbum;
import com.musicplay.musicplay.modelos.UsuarioAlbumId;
import com.musicplay.musicplay.repos.AlbumRepo;
import com.musicplay.musicplay.repos.ArtistaRepo;
import com.musicplay.musicplay.repos.CancionesAlbumRepo;
import com.musicplay.musicplay.repos.UsuarioAlbumRepo;
import com.musicplay.musicplay.repos.UsuarioRepo;
import com.musicplay.musicplay.services.ArchivoStorageService;

@RestController
public class AlbumController {

    private final AlbumRepo albumRepo;
    private final ArtistaRepo artistaRepo;
    private final CancionesAlbumRepo cancionesAlbumRepo;
    private final UsuarioRepo usuarioRepo;
    private final UsuarioAlbumRepo usuarioAlbumRepo;
    private final ArchivoStorageService storageService;

    public AlbumController(
            AlbumRepo albumRepo,
            ArtistaRepo artistaRepo,
            CancionesAlbumRepo cancionesAlbumRepo,
            UsuarioRepo usuarioRepo,
            UsuarioAlbumRepo usuarioAlbumRepo,
            ArchivoStorageService storageService) {
        this.albumRepo = albumRepo;
        this.artistaRepo = artistaRepo;
        this.cancionesAlbumRepo = cancionesAlbumRepo;
        this.usuarioRepo = usuarioRepo;
        this.usuarioAlbumRepo = usuarioAlbumRepo;
        this.storageService = storageService;
    }

    @CrossOrigin("*")
    @GetMapping("/api/albumes")
    public ResponseEntity<List<Album>> listarAlbumes() {
        return ResponseEntity.ok(albumRepo.findAll());
    }

    @CrossOrigin("*")
    @GetMapping("/api/destacados/albumes")
    public ResponseEntity<List<Album>> listarAlbumesDestacados() {
        List<Album> albumes = albumRepo.albumesMasReproducidos(PageRequest.of(0, 6));

        if (albumes.isEmpty()) {
            albumes = albumRepo.findAll(PageRequest.of(0, 6)).getContent();
        }

        return ResponseEntity.ok(albumes);
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
    @GetMapping("/api/albumes/{album_id}/canciones")
    public ResponseEntity<List<Cancion>> listarCancionesAlbum(@NonNull @PathVariable Long album_id) {
        if (!albumRepo.existsById(album_id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cancionesAlbumRepo.buscarCancionesPorAlbum(album_id));
    }

    @CrossOrigin("*")
    @DeleteMapping("/api/albumes/{album_id}/canciones/{cancion_id}")
    public ResponseEntity<?> quitarCancionAlbum(
            @NonNull @PathVariable Long album_id,
            @NonNull @PathVariable Long cancion_id) {
        CancionesAlbumId id = new CancionesAlbumId(cancion_id, album_id);

        if (!cancionesAlbumRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        cancionesAlbumRepo.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Cancion eliminada del album."));
    }

    @CrossOrigin("*")
    @GetMapping("/api/biblioteca/albumes")
    public ResponseEntity<?> listarAlbumesGuardados(Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.ok(usuarioAlbumRepo.buscarAlbumesPorUsuario(usuario.getUsuario_id()));
    }

    @CrossOrigin("*")
    @PostMapping("/api/biblioteca/albumes/{album_id}")
    public ResponseEntity<?> guardarAlbum(@NonNull @PathVariable Long album_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        if (!albumRepo.existsById(album_id)) {
            return ResponseEntity.notFound().build();
        }

        UsuarioAlbumId id = new UsuarioAlbumId(usuario.getUsuario_id(), album_id);
        if (!usuarioAlbumRepo.existsById(id)) {
            usuarioAlbumRepo.save(new UsuarioAlbum(usuario.getUsuario_id(), album_id));
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Album guardado en tu biblioteca."));
    }

    @CrossOrigin("*")
    @DeleteMapping("/api/biblioteca/albumes/{album_id}")
    public ResponseEntity<?> quitarAlbum(@NonNull @PathVariable Long album_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        usuarioAlbumRepo.deleteById(new UsuarioAlbumId(usuario.getUsuario_id(), album_id));
        return ResponseEntity.ok(Collections.singletonMap("message", "Album eliminado de tu biblioteca."));
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

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No autenticado.");
        }

        return usuarioRepo.findByUsuarioCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }
}
