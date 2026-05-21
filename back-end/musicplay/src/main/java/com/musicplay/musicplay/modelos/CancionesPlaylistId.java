package com.musicplay.musicplay.modelos;

import java.io.Serializable;
import java.util.Objects;

public class CancionesPlaylistId implements Serializable {

    private Long cancion_id;
    private Long playlist_id;

    public CancionesPlaylistId() {
    }

    public CancionesPlaylistId(Long cancion_id, Long playlist_id) {
        this.cancion_id = cancion_id;
        this.playlist_id = playlist_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CancionesPlaylistId that = (CancionesPlaylistId) o;
        return Objects.equals(cancion_id, that.cancion_id) && Objects.equals(playlist_id, that.playlist_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cancion_id, playlist_id);
    }
}
