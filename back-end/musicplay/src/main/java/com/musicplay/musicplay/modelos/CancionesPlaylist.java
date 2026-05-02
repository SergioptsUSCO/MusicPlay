package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="canciones_playlist")
public class CancionesPlaylist {

    @Id
    @Column(name="cancion_id")
    private Long cancion_id;

    @Id
    @Column(name="playlist_id")
    private Long playlist_id;

    public CancionesPlaylist(Long cancion_id, Long playlist_id) {
        super();
        this.cancion_id = cancion_id;
        this.playlist_id = playlist_id;
    }

}
