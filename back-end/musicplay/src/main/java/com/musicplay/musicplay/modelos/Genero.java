package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "genero")
@Getter
@Setter
@NoArgsConstructor
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_genero")
    private String nombre_genero;

    public Genero(Long id, String nombre_genero) {
        super();
        this.id = id;
        this.nombre_genero = nombre_genero;
    }

}
