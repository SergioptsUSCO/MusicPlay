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
@Table(name = "usuario_artista_preferido")
@IdClass(UsuarioArtistaPreferidoId.class)
public class UsuarioArtistaPreferido {

    @Id
    @Column(name = "usuario_id")
    private Long usuario_id;

    @Id
    @Column(name = "artista_id")
    private Long artista_id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artista_id", insertable = false, updatable = false)
    private Artista artista;

    public UsuarioArtistaPreferido(Long usuario_id, Long artista_id) {
        this.usuario_id = usuario_id;
        this.artista_id = artista_id;
    }
}
