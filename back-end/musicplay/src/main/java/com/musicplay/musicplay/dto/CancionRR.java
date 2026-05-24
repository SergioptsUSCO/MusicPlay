package com.musicplay.musicplay.dto;

import lombok.Getter;

//Creando clase independiente para usar en el apartado de Reproducido Recientemente
@Getter
public class CancionRR {

    private String song_nombre;
    private String song_artista;
    private String song_portada_ruta;

    public CancionRR(String song_nombre, String song_artista) {
        this.song_nombre = song_nombre;
        this.song_artista = song_artista;
    }

    public CancionRR(String song_nombre, String song_artista, String song_portada_ruta) {
        this.song_nombre = song_nombre;
        this.song_artista = song_artista;
        this.song_portada_ruta = song_portada_ruta;
    }

}
