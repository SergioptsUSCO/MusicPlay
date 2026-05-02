package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

//Generando Getter, Setter y constructores
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="album")
//Clase principal Album
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long album_id;

    @Column(name="album_nombre")
    private String album_nombre;

    @Column(name="artista_album")
    private String artista_album;

    public Album(Long album_id, String album_nombre, String artista_album){
        super();
        this.album_id = album_id;
        this.album_nombre = album_nombre;
        this.artista_album = artista_album;
    }

}
