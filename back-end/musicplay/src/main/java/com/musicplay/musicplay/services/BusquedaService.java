package com.musicplay.musicplay.services;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.musicplay.musicplay.dto.BusquedaResponse;
import com.musicplay.musicplay.modelos.Album;
import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.repos.AlbumRepo;
import com.musicplay.musicplay.repos.ArtistaRepo;
import com.musicplay.musicplay.repos.CancionRepo;

@Service
public class BusquedaService {

    private final CancionRepo cancionRepo;
    private final AlbumRepo albumRepo;
    private final ArtistaRepo artistaRepo;

    public BusquedaService(CancionRepo cancionRepo, AlbumRepo albumRepo, ArtistaRepo artistaRepo) {
        this.cancionRepo = cancionRepo;
        this.albumRepo = albumRepo;
        this.artistaRepo = artistaRepo;
    }

    public BusquedaResponse buscar(String q) {
        String query = normalize(q);

        if (query.isBlank()) {
            return new BusquedaResponse(
                    cancionRepo.findAll(),
                    albumRepo.findAll(),
                    artistaRepo.findAll()
            );
        }

        return new BusquedaResponse(
                filterAndRank(cancionRepo.findAll(), query, Cancion::getSong_nombre),
                filterAndRank(albumRepo.findAll(), query, Album::getAlbum_nombre),
                filterAndRank(artistaRepo.findAll(), query, Artista::getArtista_nombre)
        );
    }

    private <T> List<T> filterAndRank(List<T> items, String query, Function<T, String> nameGetter) {
        return items.stream()
                .filter(item -> normalize(nameGetter.apply(item)).contains(query))
                .sorted(Comparator
                        .comparingInt((T item) -> score(normalize(nameGetter.apply(item)), query))
                        .thenComparing(item -> normalize(nameGetter.apply(item))))
                .toList();
    }

    private int score(String value, String query) {
        if (value.equals(query)) {
            return 0;
        }

        if (value.startsWith(query)) {
            return 1;
        }

        return 2;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
