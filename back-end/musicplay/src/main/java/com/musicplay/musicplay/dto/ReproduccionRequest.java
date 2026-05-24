package com.musicplay.musicplay.dto;

public class ReproduccionRequest {

    private Long cancion_id;
    private Integer duracion_reproduccion;

    public Long getCancion_id() {
        return cancion_id;
    }

    public void setCancion_id(Long cancion_id) {
        this.cancion_id = cancion_id;
    }

    public Integer getDuracion_reproduccion() {
        return duracion_reproduccion;
    }

    public void setDuracion_reproduccion(Integer duracion_reproduccion) {
        this.duracion_reproduccion = duracion_reproduccion;
    }
}
