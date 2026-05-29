package com.musicplay.musicplay.modelos;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioArtistaPreferidoId implements Serializable {

    private Long usuario_id;
    private Long artista_id;

    public UsuarioArtistaPreferidoId() {
    }

    public UsuarioArtistaPreferidoId(Long usuario_id, Long artista_id) {
        this.usuario_id = usuario_id;
        this.artista_id = artista_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioArtistaPreferidoId that = (UsuarioArtistaPreferidoId) o;
        return Objects.equals(usuario_id, that.usuario_id) && Objects.equals(artista_id, that.artista_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario_id, artista_id);
    }
}
