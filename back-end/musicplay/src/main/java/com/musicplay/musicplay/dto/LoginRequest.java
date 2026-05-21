package com.musicplay.musicplay.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String usuario_correo;

    private String usuario_contraseña;

}
