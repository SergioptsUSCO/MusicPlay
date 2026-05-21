package com.musicplay.musicplay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.musicplay.musicplay.dto.RegistroRequest;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.UsuarioRepo;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepo usuarioRepository;

    @Autowired
    private PasswordEncoder encoder;

    public Usuario register(
            RegistroRequest request) {

        Usuario usuario = new Usuario();

        usuario.setUsuario_nombre(
                request.getUsuario_nombre()
        );

        usuario.setUsuario_correo(
                request.getUsuario_correo()
        );

        usuario.setUsuario_contraseña(
                encoder.encode(
                        request.getUsuario_contraseña()
                )
        );

        usuario.setUsuario_rol(2);

        return usuarioRepository.save(usuario);
    }

}
