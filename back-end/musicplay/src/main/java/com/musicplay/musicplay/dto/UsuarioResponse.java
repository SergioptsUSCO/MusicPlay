package com.musicplay.musicplay.dto;

import com.musicplay.musicplay.modelos.Usuario;

public class UsuarioResponse {

    private Long usuario_id;
    private String usuario_nombre;
    private String usuario_correo;
    private Integer usuario_rol;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Usuario usuario) {
        this.usuario_id = usuario.getUsuario_id();
        this.usuario_nombre = usuario.getUsuario_nombre();
        this.usuario_correo = usuario.getUsuario_correo();
        this.usuario_rol = usuario.getUsuario_rol();
    }

    public Long getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(Long usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getUsuario_nombre() {
        return usuario_nombre;
    }

    public void setUsuario_nombre(String usuario_nombre) {
        this.usuario_nombre = usuario_nombre;
    }

    public String getUsuario_correo() {
        return usuario_correo;
    }

    public void setUsuario_correo(String usuario_correo) {
        this.usuario_correo = usuario_correo;
    }

    public Integer getUsuario_rol() {
        return usuario_rol;
    }

    public void setUsuario_rol(Integer usuario_rol) {
        this.usuario_rol = usuario_rol;
    }
}
