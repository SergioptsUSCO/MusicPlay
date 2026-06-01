package com.musicplay.musicplay.controladores;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class AlgoritmoProxyController {

    private final String algoritmoUrl;
    private final HttpClient httpClient;

    public AlgoritmoProxyController(
            @Value("${musicplay.algoritmo-url:http://localhost:8001}") String algoritmoUrl) {
        this.algoritmoUrl = algoritmoUrl.replaceAll("/+$", "");
        this.httpClient = HttpClient.newHttpClient();
    }

    @GetMapping("/api/algoritmo/usuarios/{usuario_id}/recomendaciones")
    public ResponseEntity<?> recomendaciones(
            @PathVariable Long usuario_id,
            @RequestParam(defaultValue = "10") Integer limite) {
        String path = "/api/algoritmo/usuarios/" + usuario_id + "/recomendaciones?limite="
                + URLEncoder.encode(String.valueOf(limite), StandardCharsets.UTF_8);
        return proxyGet(path);
    }

    @PostMapping("/api/algoritmo/usuarios/{usuario_id}/historial")
    public ResponseEntity<?> historial(
            @PathVariable Long usuario_id,
            @RequestBody String body) {
        return proxyPost("/api/algoritmo/usuarios/" + usuario_id + "/historial", body);
    }

    @PostMapping("/api/algoritmo/entrenar")
    public ResponseEntity<?> entrenar() {
        return proxyPost("/api/algoritmo/entrenar", "{}");
    }

    private ResponseEntity<?> proxyGet(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(algoritmoUrl + path))
                .GET()
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        return send(request);
    }

    private ResponseEntity<?> proxyPost(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(algoritmoUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        return send(request);
    }

    private ResponseEntity<?> send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(response.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.body());
        } catch (IOException ex) {
            return ResponseEntity.status(503)
                    .body(Collections.singletonMap("error", "El servicio Python de recomendaciones no esta disponible."));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503)
                    .body(Collections.singletonMap("error", "La consulta al servicio Python fue interrumpida."));
        }
    }
}
