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
@Table(name="canciones_playlist")
@IdClass(CancionesPlaylistId.class)
public class CancionesPlaylist {

    @Id
    @Column(name="cancion_id")
    private Long cancion_id;

    @Id
    @Column(name="playlist_id")
    private Long playlist_id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancion_id", insertable = false, updatable = false)
    private Cancion cancion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private Playlist playlist;

    public CancionesPlaylist(Long cancion_id, Long playlist_id) {
        super();
        this.cancion_id = cancion_id;
        this.playlist_id = playlist_id;
    }

}
