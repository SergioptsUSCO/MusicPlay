package com.musicplay.musicplay.modelos;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioAlbumId implements Serializable {

    private Long usuario_id;
    private Long album_id;

    public UsuarioAlbumId() {
    }

    public UsuarioAlbumId(Long usuario_id, Long album_id) {
        this.usuario_id = usuario_id;
        this.album_id = album_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioAlbumId that = (UsuarioAlbumId) o;
        return Objects.equals(usuario_id, that.usuario_id) && Objects.equals(album_id, that.album_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario_id, album_id);
    }
}
