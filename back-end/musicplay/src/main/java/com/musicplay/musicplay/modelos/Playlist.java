package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "playlist")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playlist_id;

    @Column(name = "playlist_nombre")
    private String playlist_nombre;

    public Playlist(Long playlist_id, String playlist_nombre) {
        super();
        this.playlist_id = playlist_id;
        this.playlist_nombre = playlist_nombre;
    }

}
