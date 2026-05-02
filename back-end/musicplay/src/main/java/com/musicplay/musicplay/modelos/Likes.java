package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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

    public Likes(Long id, Long usuario_id, Long cancion_id) {
        super();
        this.id = id;
        this.usuario_id = usuario_id;
        this.cancion_id = cancion_id;
    }

}
