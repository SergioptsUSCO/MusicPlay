package com.musicplay.musicplay.modelos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="artista")
public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artista_id;

    @Column(name="artista_nombre")
    private String artista_nombre;


    @JsonIgnore
    @OneToMany(mappedBy = "artista", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Album> albums = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "artista", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Cancion> canciones = new ArrayList<>();

    public Artista(Long artista_id, String artista_nombre){
        super();
        this.artista_id = artista_id;
        this.artista_nombre = artista_nombre;
    }

}
