package com.musicplay.musicplay.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArchivoStorageService {

    private final Path uploadRoot;

    public ArchivoStorageService(@Value("${musicplay.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo, String carpeta) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        Path destinoCarpeta = uploadRoot.resolve(limpiarSegmento(carpeta)).normalize();
        Files.createDirectories(destinoCarpeta);

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);
        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = destinoCarpeta.resolve(nombreArchivo).normalize();

        if (!destino.startsWith(uploadRoot)) {
            throw new IOException("Ruta de archivo no permitida");
        }

        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + limpiarSegmento(carpeta) + "/" + nombreArchivo;
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "";
        }

        int punto = nombreArchivo.lastIndexOf(".");
        if (punto < 0) {
            return "";
        }

        return nombreArchivo.substring(punto).toLowerCase(Locale.ROOT);
    }

    private String limpiarSegmento(String segmento) {
        String normalizado = Normalizer.normalize(segmento, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9-_]", "-")
                .replaceAll("-+", "-")
                .toLowerCase(Locale.ROOT);

        return normalizado.isBlank() ? "archivos" : normalizado;
    }
}
