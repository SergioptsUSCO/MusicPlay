package com.musicplay.musicplay.dto;

import java.util.List;

import com.musicplay.musicplay.modelos.Album;
import com.musicplay.musicplay.modelos.Artista;
import com.musicplay.musicplay.modelos.Cancion;

public class BusquedaResponse {

    private List<Cancion> canciones;
    private List<Album> albumes;
    private List<Artista> artistas;
    private List<Artista> artistasRelacionados;

    public BusquedaResponse(List<Cancion> canciones, List<Album> albumes, List<Artista> artistas) {
        this(canciones, albumes, artistas, artistas);
    }

    public BusquedaResponse(
            List<Cancion> canciones,
            List<Album> albumes,
            List<Artista> artistas,
            List<Artista> artistasRelacionados) {
        this.canciones = canciones;
        this.albumes = albumes;
        this.artistas = artistas;
        this.artistasRelacionados = artistasRelacionados;
    }

    public List<Cancion> getCanciones() {
        return canciones;
    }

    public List<Album> getAlbumes() {
        return albumes;
    }

    public List<Artista> getArtistas() {
        return artistas;
    }

    public List<Artista> getArtistasRelacionados() {
        return artistasRelacionados;
    }
}
