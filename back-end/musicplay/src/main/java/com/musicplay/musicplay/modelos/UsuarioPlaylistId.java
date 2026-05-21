package com.musicplay.musicplay.modelos;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioPlaylistId implements Serializable {

    private Long usuario_id;
    private Long playlist_id;

    public UsuarioPlaylistId() {
    }

    public UsuarioPlaylistId(Long usuario_id, Long playlist_id) {
        this.usuario_id = usuario_id;
        this.playlist_id = playlist_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioPlaylistId that = (UsuarioPlaylistId) o;
        return Objects.equals(usuario_id, that.usuario_id) && Objects.equals(playlist_id, that.playlist_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario_id, playlist_id);
    }
}
