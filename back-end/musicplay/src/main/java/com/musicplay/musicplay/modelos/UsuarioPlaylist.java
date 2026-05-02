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
public class UsuarioPlaylist {

    @Id
    @Column(name="usuario_id")
    private Long usuario_id;

    @Id
    @Column(name="playlist_id")
    private Long playlist_id;

    public UsuarioPlaylist(Long usuario_id, Long playlist_id) {
        super();
        this.usuario_id = usuario_id;
        this.playlist_id = playlist_id;
    }

}
