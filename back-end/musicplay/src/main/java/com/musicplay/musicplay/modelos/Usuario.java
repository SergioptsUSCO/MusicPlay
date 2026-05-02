package com.musicplay.musicplay.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuario_id;

    @Column(name = "usuario_nombre")
    private String usuario_nombre;

    @Column(name = "rol_id")
    private Integer usuario_rol;

    @Column(name = "usuario_contraseña")
    private String usuario_contraseña;

    @Column(name = "usuario_correo")
    private String usuario_correo;

    public Usuario(Long usuario_id, String usuario_nombre, String usuario_contraseña, String usuario_correo, Integer rol_id) {
        super();
        this.usuario_id = usuario_id;
        this.usuario_nombre = usuario_nombre;
        this.usuario_rol = rol_id;
        this.usuario_contraseña = usuario_contraseña;
        this.usuario_correo = usuario_correo;
    }

}
