package com.musicplay.musicplay.modelos;

//importando lombok a la clase
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    Long song_id;

    @Column(name = "song_nombre")
    String song_nombre;
                                                                    
    @Column(name = "song_artista")                                     //@column para configurar
    String song_artista;                                                //El nombre de las columnas

    @Column(name = "song_compositor")
    String song_compositor;

    @Column(name = "song_genero")
    String song_genero;

    public Cancion(String song_nombre, String song_artista, String song_compositor, String song_genero) {
        super();
        this.song_nombre = song_nombre;
        this.song_artista = song_artista;                       //Constructor para la clase Cancion
        this.song_compositor = song_compositor;         
        this.song_genero = song_genero;
    }

}