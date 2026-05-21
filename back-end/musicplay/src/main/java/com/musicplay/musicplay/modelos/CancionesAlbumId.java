package com.musicplay.musicplay.modelos;

import java.io.Serializable;
import java.util.Objects;

public class CancionesAlbumId implements Serializable {

    private Long cancion_id;
    private Long album_id;

    public CancionesAlbumId() {
    }

    public CancionesAlbumId(Long cancion_id, Long album_id) {
        this.cancion_id = cancion_id;
        this.album_id = album_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CancionesAlbumId that = (CancionesAlbumId) o;
        return Objects.equals(cancion_id, that.cancion_id) && Objects.equals(album_id, that.album_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cancion_id, album_id);
    }
}
