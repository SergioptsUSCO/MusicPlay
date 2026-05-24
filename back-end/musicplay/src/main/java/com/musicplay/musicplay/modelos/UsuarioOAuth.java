package com.musicplay.musicplay.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "usuario_oauth",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_oauth_provider",
                        columnNames = {"oauth_proveedor", "oauth_proveedor_id"}
                )
        }
)
public class UsuarioOAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oauth_id;

    @Column(name = "oauth_proveedor", nullable = false)
    private String oauth_proveedor;

    @Column(name = "oauth_proveedor_id", nullable = false)
    private String oauth_proveedor_id;

    @Column(name = "oauth_correo")
    private String oauth_correo;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
