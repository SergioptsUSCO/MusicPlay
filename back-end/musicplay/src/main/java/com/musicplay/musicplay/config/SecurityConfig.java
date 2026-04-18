package com.musicplay.musicplay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // 🔹 Desactiva CSRF (útil para pruebas con Postman)
            .csrf(csrf -> csrf.disable())

            // 🔹 Configura permisos
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()   // 👈 tu API libre
                .anyRequest().authenticated()             // lo demás requiere login
            )

            // 🔹 Habilita login básico (formulario)
            .formLogin(form -> form.permitAll());

        return http.build();

    }
}
