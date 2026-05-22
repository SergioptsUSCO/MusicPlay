package com.musicplay.musicplay.services;

import java.util.Optional;

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

        if (request.getUsuario_nombre() == null || request.getUsuario_nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (request.getUsuario_correo() == null || request.getUsuario_correo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }

        Optional<Usuario> existingUserByName = usuarioRepository.findByUsuarioNombre(request.getUsuario_nombre());
        if (existingUserByName.isPresent()) {
            throw new IllegalArgumentException("Nombre de usuario no disponible, ya está en uso.");
        }

        Optional<Usuario> existingUserByEmail = usuarioRepository.findByUsuarioCorreo(request.getUsuario_correo());
        if (existingUserByEmail.isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

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
