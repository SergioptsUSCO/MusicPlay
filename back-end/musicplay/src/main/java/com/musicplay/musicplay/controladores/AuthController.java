package com.musicplay.musicplay.controladores;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.musicplay.musicplay.dto.JwtResponse;
import com.musicplay.musicplay.dto.LoginRequest;
import com.musicplay.musicplay.dto.RegistroRequest;
import com.musicplay.musicplay.security.JwtUtils;
import com.musicplay.musicplay.services.AuthService;

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroRequest request) {
        try {
            return ResponseEntity.ok(service.register(request));
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
}
