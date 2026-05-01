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
@Table(name="artista")
//Clase principal Artista
public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long artista_id;

    @Column(name="artista_nombre")
    String artista_nombre;

    @Column(name="album_artista")
    String album_artista;

    public Artista(Long artista_id, String artista_nombre, String album_artista){
        super();
        this.artista_id = artista_id;
        this.artista_nombre = artista_nombre;
        this.album_artista = album_artista;
    }

}
