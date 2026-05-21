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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "likes")
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Column(name = "cancion_id")
    private Long cancion_id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancion_id", insertable = false, updatable = false)
    private Cancion cancion;

    public Likes(Long id, Long usuario_id, Long cancion_id) {
        super();
        this.id = id;
        this.usuario_id = usuario_id;
        this.cancion_id = cancion_id;
    }

}
