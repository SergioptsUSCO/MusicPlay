package com.musicplay.musicplay.controladores;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.musicplay.musicplay.dto.PreferenciaArtistaRequest;
import com.musicplay.musicplay.dto.UsuarioResponse;
import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.modelos.UsuarioArtistaPreferido;
import com.musicplay.musicplay.modelos.UsuarioArtistaPreferidoId;
import com.musicplay.musicplay.repos.ArtistaRepo;
import com.musicplay.musicplay.repos.UsuarioArtistaPreferidoRepo;
import com.musicplay.musicplay.repos.UsuarioRepo;

@RestController
@CrossOrigin("*")
public class PreferenciasController {

    private final UsuarioRepo usuarioRepo;
    private final ArtistaRepo artistaRepo;
    private final UsuarioArtistaPreferidoRepo usuarioArtistaPreferidoRepo;

    public PreferenciasController(
            UsuarioRepo usuarioRepo,
            ArtistaRepo artistaRepo,
            UsuarioArtistaPreferidoRepo usuarioArtistaPreferidoRepo) {
        this.usuarioRepo = usuarioRepo;
        this.artistaRepo = artistaRepo;
        this.usuarioArtistaPreferidoRepo = usuarioArtistaPreferidoRepo;
    }

    @GetMapping("/api/preferencias/artistas")
    public ResponseEntity<?> listarArtistasPreferidos(Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        List<Artista> artistas = usuarioArtistaPreferidoRepo.buscarArtistasPorUsuario(usuario.getUsuario_id());
        return ResponseEntity.ok(artistas);
    }

    @PostMapping("/api/preferencias/artista-inicial")
    public ResponseEntity<?> guardarArtistaInicial(
            Authentication authentication,
            @RequestBody PreferenciaArtistaRequest request) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        if (request.getArtista_id() == null) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Debes seleccionar un artista."));
        }

        if (!artistaRepo.existsById(request.getArtista_id())) {
            return ResponseEntity.notFound().build();
        }

        UsuarioArtistaPreferidoId id = new UsuarioArtistaPreferidoId(
                usuario.getUsuario_id(),
                request.getArtista_id());

        if (!usuarioArtistaPreferidoRepo.existsById(id)) {
            usuarioArtistaPreferidoRepo.save(
                    new UsuarioArtistaPreferido(usuario.getUsuario_id(), request.getArtista_id()));
        }

        usuario.setUsuario_preferencias_configuradas(true);
        Usuario actualizado = usuarioRepo.save(usuario);
        return ResponseEntity.ok(new UsuarioResponse(actualizado));
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No autenticado.");
        }

        return usuarioRepo.findByUsuarioCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }
}
