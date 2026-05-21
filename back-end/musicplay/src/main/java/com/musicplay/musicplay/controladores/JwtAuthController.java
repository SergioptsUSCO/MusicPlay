package com.musicplay.musicplay.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.musicplay.musicplay.dto.JwtResponse;
import com.musicplay.musicplay.dto.LoginRequest;
import com.musicplay.musicplay.security.JwtUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/token")
    public ResponseEntity<JwtResponse> token(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsuario_correo(),
                        request.getUsuario_contraseña()
                )
        );

        String jwt = jwtUtils.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}
