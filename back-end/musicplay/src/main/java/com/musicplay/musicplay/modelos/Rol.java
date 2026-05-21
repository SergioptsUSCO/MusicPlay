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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rol_id;

    @Column(name = "rol_nombre")
    private String rol_nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();

    public Rol(Integer rol_id, String rol_nombre) {
        super();
        this.rol_id = rol_id;
        this.rol_nombre = rol_nombre;
    }
}
