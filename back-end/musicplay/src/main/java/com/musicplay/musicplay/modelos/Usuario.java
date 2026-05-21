package com.musicplay.musicplay.modelos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", insertable = false, updatable = false)
    private Rol rol;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Playlist> playlists = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistorialReproduccion> historialReproduccion = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Likes> likes = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_playlist",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "playlist_id")
    )
    private List<Playlist> sharedPlaylists = new ArrayList<>();

    public Usuario(Long usuario_id, String usuario_nombre, String usuario_contraseña, String usuario_correo, Integer rol_id) {
        super();
        this.usuario_id = usuario_id;
        this.usuario_nombre = usuario_nombre;
        this.usuario_rol = rol_id;
        this.usuario_contraseña = usuario_contraseña;
        this.usuario_correo = usuario_correo;
    }

}
