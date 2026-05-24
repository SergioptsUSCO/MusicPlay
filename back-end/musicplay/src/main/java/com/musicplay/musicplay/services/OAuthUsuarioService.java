package com.musicplay.musicplay.services;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.modelos.UsuarioOAuth;
import com.musicplay.musicplay.repos.UsuarioOAuthRepo;
import com.musicplay.musicplay.repos.UsuarioRepo;

@Service
public class OAuthUsuarioService {

    private final UsuarioRepo usuarioRepository;
    private final UsuarioOAuthRepo usuarioOAuthRepository;
    private final PasswordEncoder encoder;

    public OAuthUsuarioService(
            UsuarioRepo usuarioRepository,
            UsuarioOAuthRepo usuarioOAuthRepository,
            PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioOAuthRepository = usuarioOAuthRepository;
        this.encoder = encoder;
    }

    @Transactional
    public Usuario findOrCreateUsuario(String proveedor, Map<String, Object> attributes) {
        String proveedorId = getRequiredAttribute(attributes, "sub");
        String correo = getRequiredAttribute(attributes, "email");
        String nombre = getAttribute(attributes, "name");

        return usuarioOAuthRepository
                .findByOauthProveedorAndOauthProveedorId(proveedor, proveedorId)
                .map(UsuarioOAuth::getUsuario)
                .orElseGet(() -> linkOAuthUsuario(proveedor, proveedorId, correo, nombre));
    }

    private Usuario linkOAuthUsuario(
            String proveedor,
            String proveedorId,
            String correo,
            String nombre) {

        Usuario usuario = usuarioRepository
                .findByUsuarioCorreo(correo)
                .orElseGet(() -> createOAuthUsuario(correo, nombre));

        UsuarioOAuth oauth = new UsuarioOAuth();
        oauth.setOauth_proveedor(proveedor);
        oauth.setOauth_proveedor_id(proveedorId);
        oauth.setOauth_correo(correo);
        oauth.setUsuario(usuario);

        usuarioOAuthRepository.save(oauth);
        return usuario;
    }

    private Usuario createOAuthUsuario(String correo, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setUsuario_nombre(buildAvailableUsername(nombre, correo));
        usuario.setUsuario_correo(correo);
        usuario.setUsuario_contraseña(encoder.encode(UUID.randomUUID().toString()));
        usuario.setUsuario_rol(2);
        return usuarioRepository.save(usuario);
    }

    private String buildAvailableUsername(String nombre, String correo) {
        String base = nombre != null && !nombre.isBlank()
                ? nombre.trim()
                : correo.substring(0, correo.indexOf("@"));

        String candidate = base;
        int suffix = 1;

        while (usuarioRepository.findByUsuarioNombre(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    private String getRequiredAttribute(Map<String, Object> attributes, String name) {
        String value = getAttribute(attributes, name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Google no entrego el atributo obligatorio: " + name);
        }
        return value;
    }

    private String getAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value == null ? null : value.toString();
    }
}
