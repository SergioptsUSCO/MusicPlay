package com.musicplay.musicplay.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(2)
public class JwtSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http)
            throws Exception {

        http
                .securityMatcher("/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/canciones",
                                "/api/buscarCancion/**",
                                "/api/canciones/**",
                                "/api/artistas",
                                "/api/buscarArtista/**",
                                "/api/albumes",
                                "/api/albumes/**",
                                "/api/buscarAlbum/**",
                                "/api/recientementeReproducidas",
                                "/api/busqueda"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reproducciones").authenticated()
                        .requestMatchers(
                                "/api/crearCancion",
                                "/api/actualizarCancion/**",
                                "/api/eliminarCancion/**",
                                "/api/crearArtista",
                                "/api/actualizarArtista/**",
                                "/api/eliminarArtista/**",
                                "/api/crearAlbum",
                                "/api/actualizarAlbum/**",
                                "/api/eliminarAlbum/**"
                        ).hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
