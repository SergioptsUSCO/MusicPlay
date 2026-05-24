package com.musicplay.musicplay.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.musicplay.musicplay.dto.BusquedaResponse;
import com.musicplay.musicplay.services.BusquedaService;

@RestController
public class BusquedaController {

    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    @CrossOrigin("*")
    @GetMapping("/api/busqueda")
    public ResponseEntity<BusquedaResponse> buscar(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(busquedaService.buscar(q));
    }
}
