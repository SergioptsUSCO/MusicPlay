package com.musicplay.musicplay.modelos;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioGeneroPreferidoId implements Serializable {

    private Long usuario_id;
    private Long genero_id;

    public UsuarioGeneroPreferidoId() {
    }

    public UsuarioGeneroPreferidoId(Long usuario_id, Long genero_id) {
        this.usuario_id = usuario_id;
        this.genero_id = genero_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioGeneroPreferidoId that = (UsuarioGeneroPreferidoId) o;
        return Objects.equals(usuario_id, that.usuario_id) && Objects.equals(genero_id, that.genero_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario_id, genero_id);
    }
}
