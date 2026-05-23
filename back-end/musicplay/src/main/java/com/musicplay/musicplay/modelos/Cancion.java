package com.musicplay.musicplay.modelos;

//importando lombok a la clase
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//Generando Getter, Setter y constructores
@NoArgsConstructor
@Getter
@Setter
@Table(name="song")

//Clase principal Cancion
@Entity         //Configurando la entidad
public class Cancion {
    
    @Id         //Definiendo ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)       //Id se va auto-incrementando
    private Long song_id;

    @Column(name = "song_nombre")
    private String song_nombre;
                                                                    
    @Column(name = "song_artista")                                     //@column para configurar
    private Long song_artista;                                                //El id del artista

    @Column(name = "song_compositor")
    private String song_compositor;

    @Column(name = "song_genero")
    private Long song_genero;

    @Column(name = "song_archivo_ruta")
    private String song_archivo_ruta;

    @Column(name = "song_portada_ruta")
    private String song_portada_ruta;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_artista", referencedColumnName = "artista_id", insertable = false, updatable = false)
    private Artista artista;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_genero", referencedColumnName = "id", insertable = false, updatable = false)
    private Genero genero;

    @JsonIgnore
    @ManyToMany(mappedBy = "canciones", fetch = FetchType.LAZY)
    private List<Playlist> playlists = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "canciones_album",
        joinColumns = @JoinColumn(name = "cancion_id"),
        inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    private List<Album> albums = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CancionesPlaylist> cancionesPlaylist = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CancionesAlbum> cancionesAlbum = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Likes> likes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistorialReproduccion> historiales = new ArrayList<>();

    public Cancion(String song_nombre, Long song_artista, String song_compositor, Long song_genero) {
        super();
        this.song_nombre = song_nombre;
        this.song_artista = song_artista;                       //Constructor para la clase Cancion
        this.song_compositor = song_compositor;         
        this.song_genero = song_genero;
    }

}
