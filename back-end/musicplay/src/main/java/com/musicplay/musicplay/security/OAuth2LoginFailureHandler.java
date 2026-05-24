package com.musicplay.musicplay.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final String oauthFailureUrl;

    public OAuth2LoginFailureHandler(
            @Value("${musicplay.oauth.failure-url:http://localhost:5500/front-end/page/login.html}") String oauthFailureUrl) {
        this.oauthFailureUrl = oauthFailureUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String redirectUrl = UriComponentsBuilder
                .fromUriString(oauthFailureUrl)
                .queryParam("oauthError", exception.getMessage())
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
