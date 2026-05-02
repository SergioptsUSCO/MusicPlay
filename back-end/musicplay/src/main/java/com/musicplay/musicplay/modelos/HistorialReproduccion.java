package com.musicplay.musicplay.modelos;

import java.time.LocalDateTime;

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
@Table(name = "historial_reproduccion")
public class HistorialReproduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Column(name = "cancion_id")
    private Long cancion_id;

    @Column(name = "fecha_reproduccion")
    private LocalDateTime fecha_reproduccion;

    @Column(name = "duracion_reproduccion")
    private Integer duracion_reproduccion;

    public HistorialReproduccion(Long id, Long usuario_id, Long cancion_id, LocalDateTime fecha_reproduccion,
            Integer duracion_reproduccion) {
        super();
        this.id = id;
        this.usuario_id = usuario_id;
        this.cancion_id = cancion_id;
        this.fecha_reproduccion = fecha_reproduccion;
        this.duracion_reproduccion = duracion_reproduccion;
    }
    
}
