package com.musicplay.musicplay.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.services.OAuthUsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final OAuthUsuarioService oauthUsuarioService;
    private final JwtUtils jwtUtils;
    private final String oauthSuccessUrl;

    public OAuth2LoginSuccessHandler(
            OAuthUsuarioService oauthUsuarioService,
            JwtUtils jwtUtils,
            @Value("${musicplay.oauth.success-url:http://localhost:5500/front-end/page/home.html}") String oauthSuccessUrl) {
        this.oauthUsuarioService = oauthUsuarioService;
        this.jwtUtils = jwtUtils;
        this.oauthSuccessUrl = oauthSuccessUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Usuario usuario = oauthUsuarioService.findOrCreateUsuario(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getPrincipal().getAttributes()
        );

        String role = usuario.getUsuario_rol() == 1 ? "ROLE_ADMIN" : "ROLE_USER";
        UsernamePasswordAuthenticationToken jwtAuthentication =
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsuario_correo(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );

        String token = jwtUtils.generateToken(jwtAuthentication);
        String redirectUrl = UriComponentsBuilder
                .fromUriString(oauthSuccessUrl)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
