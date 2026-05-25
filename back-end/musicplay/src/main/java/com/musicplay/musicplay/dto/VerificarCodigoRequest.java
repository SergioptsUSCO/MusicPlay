package com.musicplay.musicplay.dto;

import lombok.Data;

@Data
public class VerificarCodigoRequest {

    private String correo;

    private String codigo;
}
