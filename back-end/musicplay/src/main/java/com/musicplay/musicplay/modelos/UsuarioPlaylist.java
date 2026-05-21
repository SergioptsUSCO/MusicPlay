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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="usuario_playlist")
@IdClass(UsuarioPlaylistId.class)
public class UsuarioPlaylist {

    @Id
    @Column(name="usuario_id")
    private Long usuario_id;

    @Id
    @Column(name="playlist_id")
    private Long playlist_id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private Playlist playlist;

    public UsuarioPlaylist(Long usuario_id, Long playlist_id) {
        super();
        this.usuario_id = usuario_id;
        this.playlist_id = playlist_id;
    }

}
