package com.musicplay.musicplay.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="canciones_album")
@IdClass(CancionesAlbumId.class)
public class CancionesAlbum {

    @Id
    @Column(name="cancion_id")
    private Long cancion_id;

    @Id
    @Column(name="album_id")
    private Long album_id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancion_id", insertable = false, updatable = false)
    private Cancion cancion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

}
