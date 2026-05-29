package com.musicplay.musicplay.controladores;

import java.util.List;
import java.util.Optional;

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

import com.musicplay.musicplay.modelos.Genero;
import com.musicplay.musicplay.repos.GeneroRepo;

@RestController
public class GeneroController {

    private final GeneroRepo generoRepo;

    public GeneroController(GeneroRepo generoRepo) {
        this.generoRepo = generoRepo;
    }

    @CrossOrigin("*")
    @GetMapping("/api/generos")
    public ResponseEntity<List<Genero>> listarGeneros() {
        return ResponseEntity.ok(generoRepo.findAll());
    }

    @CrossOrigin("*")
    @GetMapping("/api/buscarGenero/{genero_id}")
    public ResponseEntity<Genero> obtenerGenero(@NonNull @PathVariable Long genero_id) {
        return generoRepo.findById(genero_id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @CrossOrigin("*")
    @PostMapping("/api/crearGenero")
    public ResponseEntity<?> crearGenero(@RequestParam String nombre_genero) {
        if (nombre_genero == null || nombre_genero.isBlank()) {
            return ResponseEntity.badRequest().body("El nombre del genero es obligatorio.");
        }

        Optional<Genero> existente = generoRepo.buscarPorNombre(nombre_genero.trim());
        if (existente.isPresent()) {
            return ResponseEntity.badRequest().body("Ya existe un genero con ese nombre.");
        }

        Genero genero = new Genero();
        genero.setNombre_genero(nombre_genero.trim());
        return ResponseEntity.ok(generoRepo.save(genero));
    }

    @CrossOrigin("*")
    @PutMapping("/api/actualizarGenero/{genero_id}")
    public ResponseEntity<?> actualizarGenero(
            @PathVariable @NonNull Long genero_id,
            @RequestParam String nombre_genero) {

        if (nombre_genero == null || nombre_genero.isBlank()) {
            return ResponseEntity.badRequest().body("El nombre del genero es obligatorio.");
        }

        Optional<Genero> opt = generoRepo.findById(genero_id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Genero> existente = generoRepo.buscarPorNombre(nombre_genero.trim());
        if (existente.isPresent() && !existente.get().getId().equals(genero_id)) {
            return ResponseEntity.badRequest().body("Ya existe un genero con ese nombre.");
        }

        Genero genero = opt.get();
        genero.setNombre_genero(nombre_genero.trim());
        return ResponseEntity.ok(generoRepo.save(genero));
    }

    @CrossOrigin("*")
    @DeleteMapping("/api/eliminarGenero/{genero_id}")
    public ResponseEntity<Genero> eliminarGenero(@PathVariable @NonNull Long genero_id) {
        Optional<Genero> opt = generoRepo.findById(genero_id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        generoRepo.delete(opt.get());
        return ResponseEntity.ok(opt.get());
    }
}
