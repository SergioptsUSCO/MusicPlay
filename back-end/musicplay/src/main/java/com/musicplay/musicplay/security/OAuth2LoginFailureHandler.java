package com.musicplay.musicplay.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

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

        logger.error("OAuth2 login failed", exception);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(oauthFailureUrl)
                .queryParam("oauthError", exception.getMessage())
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
