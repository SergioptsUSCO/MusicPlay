package com.musicplay.musicplay.modelos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="album")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long album_id;

    @Column(name="album_nombre")
    private String album_nombre;

    @Column(name="artista_album")
    private Long artista_album;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artista_album", referencedColumnName = "artista_id", insertable = false, updatable = false)
    private Artista artista;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "canciones_album",
        joinColumns = @JoinColumn(name = "album_id"),
        inverseJoinColumns = @JoinColumn(name = "cancion_id")
    )
    private List<Cancion> canciones = new ArrayList<>();

    public Album(Long album_id, String album_nombre, Long artista_album){
        super();
        this.album_id = album_id;
        this.album_nombre = album_nombre;
        this.artista_album = artista_album;
    }

}
