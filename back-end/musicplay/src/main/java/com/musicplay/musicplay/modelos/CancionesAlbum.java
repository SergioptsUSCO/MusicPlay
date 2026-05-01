package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

//Generando Getter, Setter y constructores
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="canciones_album")
//Clase principal CancionesAlbum
public class CancionesAlbum {

    @Id
    @Column(name="cancion_id")
    Long cancion_id;

    @Id
    @Column(name="album_id")
    Long album_id; 

}
