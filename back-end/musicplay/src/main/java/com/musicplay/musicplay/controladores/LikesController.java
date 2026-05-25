package com.musicplay.musicplay.controladores;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicplay.musicplay.modelos.Likes;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.CancionRepo;
import com.musicplay.musicplay.repos.LikesRepo;
import com.musicplay.musicplay.repos.UsuarioRepo;

@RestController
@CrossOrigin("*")
public class LikesController {

    private final LikesRepo likesRepo;
    private final UsuarioRepo usuarioRepo;
    private final CancionRepo cancionRepo;

    public LikesController(LikesRepo likesRepo, UsuarioRepo usuarioRepo, CancionRepo cancionRepo) {
        this.likesRepo = likesRepo;
        this.usuarioRepo = usuarioRepo;
        this.cancionRepo = cancionRepo;
    }

    @GetMapping("/api/likes")
    public ResponseEntity<?> listarLikes(Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.ok(likesRepo.buscarCancionesIdsPorUsuario(usuario.getUsuario_id()));
    }

    @GetMapping("/api/likes/canciones")
    public ResponseEntity<?> listarCancionesLike(Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.ok(likesRepo.buscarCancionesPorUsuario(usuario.getUsuario_id()));
    }

    @PostMapping("/api/likes/{cancion_id}")
    public ResponseEntity<?> darLike(@PathVariable Long cancion_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        if (!cancionRepo.existsById(cancion_id)) {
            return ResponseEntity.notFound().build();
        }

        if (!likesRepo.existePorUsuarioYCancion(usuario.getUsuario_id(), cancion_id)) {
            likesRepo.save(new Likes(null, usuario.getUsuario_id(), cancion_id));
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Cancion agregada a tus gustos."));
    }

    @DeleteMapping("/api/likes/{cancion_id}")
    public ResponseEntity<?> quitarLike(@PathVariable Long cancion_id, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        likesRepo.buscarPorUsuarioYCancion(usuario.getUsuario_id(), cancion_id)
                .ifPresent(likesRepo::delete);
        return ResponseEntity.ok(Collections.singletonMap("message", "Cancion eliminada de tus gustos."));
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No autenticado.");
        }

        return usuarioRepo.findByUsuarioCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }
}
