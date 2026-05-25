package com.musicplay.musicplay.controladores;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.modelos.CancionesPlaylist;
import com.musicplay.musicplay.modelos.CancionesPlaylistId;
import com.musicplay.musicplay.modelos.Playlist;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.CancionRepo;
import com.musicplay.musicplay.repos.CancionesPlaylistRepo;
import com.musicplay.musicplay.repos.PlaylistRepo;
import com.musicplay.musicplay.repos.UsuarioRepo;
import com.musicplay.musicplay.services.ArchivoStorageService;

@RestController
@CrossOrigin("*")
public class PlaylistController {

    private final PlaylistRepo playlistRepo;
    private final CancionRepo cancionRepo;
    private final CancionesPlaylistRepo cancionesPlaylistRepo;
    private final UsuarioRepo usuarioRepo;
    private final ArchivoStorageService storageService;

    public PlaylistController(
            PlaylistRepo playlistRepo,
            CancionRepo cancionRepo,
            CancionesPlaylistRepo cancionesPlaylistRepo,
            UsuarioRepo usuarioRepo,
            ArchivoStorageService storageService) {
        this.playlistRepo = playlistRepo;
        this.cancionRepo = cancionRepo;
        this.cancionesPlaylistRepo = cancionesPlaylistRepo;
        this.usuarioRepo = usuarioRepo;
        this.storageService = storageService;
    }

    @GetMapping("/api/playlists")
    public ResponseEntity<List<Playlist>> listarPlaylists(Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.ok(playlistRepo.buscarPorUsuario(usuario.getUsuario_id()));
    }

    @GetMapping("/api/playlists/{playlist_id}")
    public ResponseEntity<Playlist> obtenerPlaylist(@NonNull @PathVariable Long playlist_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return playlistRepo.findById(playlist_id)
                .filter(playlist -> playlist.getUsuario_id().equals(usuario.getUsuario_id()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/playlists/{playlist_id}/canciones")
    public ResponseEntity<List<Cancion>> listarCancionesPlaylist(@NonNull @PathVariable Long playlist_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        boolean belongsToUser = playlistRepo.findById(playlist_id)
                .map(playlist -> playlist.getUsuario_id().equals(usuario.getUsuario_id()))
                .orElse(false);

        if (!belongsToUser) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cancionesPlaylistRepo.buscarCancionesPorPlaylist(playlist_id));
    }

    @PostMapping(value = "/api/playlists", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Playlist> crearPlaylist(
            @RequestParam String playlist_nombre,
            @RequestParam(required = false) MultipartFile playlist_portada,
            Authentication authentication) throws IOException {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        if (playlist_nombre == null || playlist_nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la playlist es obligatorio.");
        }

        Playlist playlist = new Playlist();
        playlist.setPlaylist_nombre(playlist_nombre.trim());
        playlist.setUsuario_id(usuario.getUsuario_id());
        playlist.setPlaylist_portada_ruta(storageService.guardar(playlist_portada, "portadas-playlists"));
        return ResponseEntity.ok(playlistRepo.save(playlist));
    }

    @PutMapping(value = "/api/playlists/{playlist_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Playlist> actualizarPlaylist(
            @PathVariable Long playlist_id,
            @RequestParam String playlist_nombre,
            @RequestParam(required = false) MultipartFile playlist_portada,
            Authentication authentication) throws IOException {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        if (playlist_nombre == null || playlist_nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la playlist es obligatorio.");
        }

        return playlistRepo.findById(playlist_id)
                .filter(playlist -> playlist.getUsuario_id().equals(usuario.getUsuario_id()))
                .map(playlist -> {
                    try {
                        playlist.setPlaylist_nombre(playlist_nombre.trim());
                        String rutaPortada = storageService.guardar(playlist_portada, "portadas-playlists");
                        if (rutaPortada != null) {
                            playlist.setPlaylist_portada_ruta(rutaPortada);
                        }
                        return ResponseEntity.ok(playlistRepo.save(playlist));
                    } catch (IOException exception) {
                        throw new IllegalArgumentException("No se pudo guardar la portada.");
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/playlists/{playlist_id}")
    public ResponseEntity<?> eliminarPlaylist(@PathVariable Long playlist_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return playlistRepo.findById(playlist_id)
                .filter(playlist -> playlist.getUsuario_id().equals(usuario.getUsuario_id()))
                .map(playlist -> {
                    cancionesPlaylistRepo.deleteAll(cancionesPlaylistRepo.buscarPorPlaylist(playlist_id));
                    playlistRepo.delete(playlist);
                    return ResponseEntity.ok(Collections.singletonMap("message", "Playlist eliminada."));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/playlists/{playlist_id}/canciones/{cancion_id}")
    public ResponseEntity<?> agregarCancion(
            @PathVariable Long playlist_id,
            @PathVariable Long cancion_id,
            Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        boolean belongsToUser = playlistRepo.findById(playlist_id)
                .map(playlist -> playlist.getUsuario_id().equals(usuario.getUsuario_id()))
                .orElse(false);

        if (!belongsToUser || !cancionRepo.existsById(cancion_id)) {
            return ResponseEntity.notFound().build();
        }

        CancionesPlaylistId id = new CancionesPlaylistId(cancion_id, playlist_id);
        if (!cancionesPlaylistRepo.existsById(id)) {
            cancionesPlaylistRepo.save(new CancionesPlaylist(cancion_id, playlist_id));
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Cancion agregada a la playlist."));
    }

    @DeleteMapping("/api/playlists/{playlist_id}/canciones/{cancion_id}")
    public ResponseEntity<?> eliminarCancion(
            @PathVariable Long playlist_id,
            @PathVariable Long cancion_id,
            Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        boolean belongsToUser = playlistRepo.findById(playlist_id)
                .map(playlist -> playlist.getUsuario_id().equals(usuario.getUsuario_id()))
                .orElse(false);

        if (!belongsToUser) {
            return ResponseEntity.notFound().build();
        }

        CancionesPlaylistId id = new CancionesPlaylistId(cancion_id, playlist_id);
        if (!cancionesPlaylistRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        cancionesPlaylistRepo.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Cancion eliminada de la playlist."));
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No autenticado.");
        }

        return usuarioRepo.findByUsuarioCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }
}
