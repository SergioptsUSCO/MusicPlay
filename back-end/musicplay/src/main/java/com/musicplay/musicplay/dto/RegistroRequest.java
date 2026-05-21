package com.musicplay.musicplay.dto;

import lombok.Data;

@Data
public class RegistroRequest {

    private String usuario_nombre;

    private String usuario_correo;

    private String usuario_contraseña;

}
