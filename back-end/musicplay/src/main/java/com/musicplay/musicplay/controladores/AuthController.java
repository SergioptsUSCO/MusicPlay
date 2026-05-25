package com.musicplay.musicplay.controladores;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.musicplay.musicplay.dto.JwtResponse;
import com.musicplay.musicplay.dto.LoginRequest;
import com.musicplay.musicplay.dto.CambiarContrasenaRequest;
import com.musicplay.musicplay.dto.CorreoRequest;
import com.musicplay.musicplay.dto.RegistroRequest;
import com.musicplay.musicplay.dto.UsuarioResponse;
import com.musicplay.musicplay.dto.VerificarCodigoRequest;
import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.UsuarioRepo;
import com.musicplay.musicplay.security.JwtUtils;
import com.musicplay.musicplay.services.AuthService;
import com.musicplay.musicplay.services.PasswordRecoveryService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UsuarioRepo usuarioRepository;

    @Autowired
    private PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroRequest request) {
        try {
            Usuario usuario = service.register(request);
            return ResponseEntity.ok(new UsuarioResponse(usuario));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {
        try {
            Authentication authentication = manager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsuario_correo(),
                            request.getUsuario_contraseña()
                    )
            );

            String jwt = jwtUtils.generateToken(authentication);
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("error", "Usuario o contraseña incorrectos."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody CorreoRequest request) {
        passwordRecoveryService.solicitarCodigo(request.getCorreo());
        return ResponseEntity.ok(Collections.singletonMap("message", "Codigo enviado al correo."));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody VerificarCodigoRequest request) {
        passwordRecoveryService.verificarCodigo(request.getCorreo(), request.getCodigo());
        return ResponseEntity.ok(Collections.singletonMap("message", "Codigo verificado."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody CambiarContrasenaRequest request) {
        passwordRecoveryService.cambiarContrasena(
                request.getCorreo(),
                request.getCodigo(),
                request.getNuevaContrasena()
        );
        return ResponseEntity.ok(Collections.singletonMap("message", "Contrasena actualizada correctamente."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("error", "No autenticado."));
        }

        return usuarioRepository
                .findByUsuarioCorreo(authentication.getName())
                .<ResponseEntity<?>>map(usuario -> ResponseEntity.ok(new UsuarioResponse(usuario)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Collections.singletonMap("error", "Usuario no encontrado.")));
    }
}
