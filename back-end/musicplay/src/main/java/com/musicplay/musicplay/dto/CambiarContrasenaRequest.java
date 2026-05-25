package com.musicplay.musicplay.dto;

import lombok.Data;

@Data
public class CambiarContrasenaRequest {

    private String correo;

    private String codigo;

    private String nuevaContrasena;
}
