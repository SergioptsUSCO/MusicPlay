package com.musicplay.musicplay.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicplay.musicplay.dto.ReproduccionRequest;
import com.musicplay.musicplay.modelos.HistorialReproduccion;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.CancionRepo;
import com.musicplay.musicplay.repos.HistorialReproduccionRepo;
import com.musicplay.musicplay.repos.UsuarioRepo;

@Service
public class HistorialReproduccionService {

    private final HistorialReproduccionRepo historialRepo;
    private final UsuarioRepo usuarioRepo;
    private final CancionRepo cancionRepo;

    public HistorialReproduccionService(
            HistorialReproduccionRepo historialRepo,
            UsuarioRepo usuarioRepo,
            CancionRepo cancionRepo) {
        this.historialRepo = historialRepo;
        this.usuarioRepo = usuarioRepo;
        this.cancionRepo = cancionRepo;
    }

    @Transactional
    public void registrar(String usuarioCorreo, ReproduccionRequest request) {
        if (request.getCancion_id() == null) {
            throw new IllegalArgumentException("La cancion es obligatoria.");
        }

        Usuario usuario = usuarioRepo.findByUsuarioCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!cancionRepo.existsById(request.getCancion_id())) {
            throw new IllegalArgumentException("La cancion no existe.");
        }

        HistorialReproduccion historial = new HistorialReproduccion();
        historial.setUsuario_id(usuario.getUsuario_id());
        historial.setCancion_id(request.getCancion_id());
        historial.setFecha_reproduccion(LocalDateTime.now());
        historial.setDuracion_reproduccion(safeDuration(request.getDuracion_reproduccion()));

        historialRepo.save(historial);
    }

    private Integer safeDuration(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }

        return value;
    }
}
