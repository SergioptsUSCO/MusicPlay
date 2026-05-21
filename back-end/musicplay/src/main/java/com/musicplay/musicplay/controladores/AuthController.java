package com.musicplay.musicplay.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.musicplay.musicplay.dto.LoginRequest;
import com.musicplay.musicplay.dto.RegistroRequest;
import com.musicplay.musicplay.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager manager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroRequest request) {

        return ResponseEntity.ok(
                service.register(request)
        );

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        manager.authenticate( new UsernamePasswordAuthenticationToken(
                        request.getUsuario_correo(),
                        request.getUsuario_contraseña()
                )
        );

        return ResponseEntity.ok(
                "Login exitoso"
        );
    }
}
