package com.musicplay.musicplay.services;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.UsuarioRepo;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.*;

import org.springframework.stereotype.Service;

@Service("servicesCustomUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepo usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(
            String correo)
            throws UsernameNotFoundException {

        Usuario usuario =
                usuarioRepository
                        .findByUsuarioCorreo(correo)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Usuario no encontrado"
                                ));

        String role =
                usuario.getUsuario_rol() == 1
                        ? "ADMIN"
                        : "USER";

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(usuario.getUsuario_correo())
                .password(usuario.getUsuario_contraseña())
                .roles(role)
                .build();
    }

}
