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
@Table(name = "usuario_genero_preferido")
@IdClass(UsuarioGeneroPreferidoId.class)
public class UsuarioGeneroPreferido {

    @Id
    @Column(name = "usuario_id")
    private Long usuario_id;

    @Id
    @Column(name = "genero_id")
    private Long genero_id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genero_id", insertable = false, updatable = false)
    private Genero genero;

    public UsuarioGeneroPreferido(Long usuario_id, Long genero_id) {
        this.usuario_id = usuario_id;
        this.genero_id = genero_id;
    }
}
