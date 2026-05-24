package com.musicplay.musicplay.controladores;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.musicplay.musicplay.dto.ReproduccionRequest;
import com.musicplay.musicplay.services.HistorialReproduccionService;

@RestController
public class HistorialReproduccionController {

    private final HistorialReproduccionService historialService;

    public HistorialReproduccionController(HistorialReproduccionService historialService) {
        this.historialService = historialService;
    }

    @CrossOrigin("*")
    @PostMapping("/api/reproducciones")
    public ResponseEntity<?> registrar(
            Authentication authentication,
            @RequestBody ReproduccionRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("error", "No autenticado."));
        }

        try {
            historialService.registrar(authentication.getName(), request);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }
}
