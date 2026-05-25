package com.musicplay.musicplay.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.musicplay.musicplay.modelos.Usuario;
import com.musicplay.musicplay.repos.UsuarioRepo;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class PasswordRecoveryService {

    private static final int CODE_TTL_MINUTES = 10;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

    private final UsuarioRepo usuarioRepository;
    private final PasswordEncoder encoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, RecoveryCode> recoveryCodes = new ConcurrentHashMap<>();

    @Value("${musicplay.mail.host:}")
    private String mailHost;

    @Value("${musicplay.mail.port:587}")
    private int mailPort;

    @Value("${musicplay.mail.username:}")
    private String mailUsername;

    @Value("${musicplay.mail.password:}")
    private String mailPassword;

    @Value("${musicplay.mail.from:${musicplay.mail.username:}}")
    private String mailFrom;

    @Value("${musicplay.mail.auth:true}")
    private boolean mailAuth;

    @Value("${musicplay.mail.starttls:true}")
    private boolean mailStarttls;

    @Value("${musicplay.mail.dev-code-fallback:true}")
    private boolean devCodeFallback;

    public PasswordRecoveryService(UsuarioRepo usuarioRepository, PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
    }

    public void solicitarCodigo(String correo) {
        String normalizedEmail = normalizeEmail(correo);

        usuarioRepository.findByUsuarioCorreo(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese correo."));

        String codigo = String.format("%06d", secureRandom.nextInt(1_000_000));
        recoveryCodes.put(normalizedEmail, new RecoveryCode(codigo, Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES)));

        enviarCodigo(normalizedEmail, codigo);
    }

    public void verificarCodigo(String correo, String codigo) {
        validarCodigo(normalizeEmail(correo), codigo);
    }

    public void cambiarContrasena(String correo, String codigo, String nuevaContrasena) {
        String normalizedEmail = normalizeEmail(correo);

        if (nuevaContrasena == null || !nuevaContrasena.matches(PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial.");
        }

        validarCodigo(normalizedEmail, codigo);

        Usuario usuario = usuarioRepository.findByUsuarioCorreo(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        usuario.setUsuario_contraseña(encoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        recoveryCodes.remove(normalizedEmail);
    }

    private String normalizeEmail(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }

        return correo.trim().toLowerCase();
    }

    private void validarCodigo(String correo, String codigo) {
        if (codigo == null || !codigo.matches("\\d{6}")) {
            throw new IllegalArgumentException("El codigo debe tener 6 digitos.");
        }

        RecoveryCode recoveryCode = recoveryCodes.get(correo);

        if (recoveryCode == null) {
            throw new IllegalArgumentException("Solicita un nuevo codigo de verificacion.");
        }

        if (recoveryCode.expiresAt().isBefore(Instant.now())) {
            recoveryCodes.remove(correo);
            throw new IllegalArgumentException("El codigo expiro. Solicita uno nuevo.");
        }

        if (!recoveryCode.code().equals(codigo)) {
            throw new IllegalArgumentException("Codigo de verificacion incorrecto.");
        }
    }

    private void enviarCodigo(String correo, String codigo) {
        if (!isMailConfigured()) {
            if (devCodeFallback) {
                logDevCode(correo, codigo);
                return;
            }

            throw new IllegalArgumentException("Configura MUSICPLAY_MAIL_HOST, MUSICPLAY_MAIL_FROM, MUSICPLAY_MAIL_USERNAME y MUSICPLAY_MAIL_PASSWORD para enviar correos.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailHost);
        properties.put("mail.smtp.port", String.valueOf(mailPort));
        properties.put("mail.smtp.auth", String.valueOf(mailAuth));
        properties.put("mail.smtp.starttls.enable", String.valueOf(mailStarttls));
        properties.put("mail.smtp.ssl.trust", mailHost);
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername.trim(), mailPassword.replace(" ", ""));
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correo));
            message.setSubject("Codigo de recuperacion - Musicplay");
            message.setText("""
                    Tu codigo de recuperacion de Musicplay es: %s

                    Este codigo vence en %d minutos.
                    Si no solicitaste este cambio, ignora este correo.
                    """.formatted(codigo, CODE_TTL_MINUTES));

            Transport.send(message);
        } catch (MessagingException exception) {
            if (devCodeFallback) {
                logDevCode(correo, codigo);
                return;
            }

            throw new IllegalArgumentException("No se pudo enviar el correo de recuperacion. Detalle SMTP: " + exception.getMessage());
        }
    }

    private boolean isMailConfigured() {
        return hasRealValue(mailHost)
                && hasRealValue(mailFrom)
                && hasRealValue(mailUsername)
                && hasRealValue(mailPassword);
    }

    private boolean hasRealValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalizedValue = value.trim().toLowerCase();
        return !normalizedValue.startsWith("tu-")
                && !normalizedValue.contains("contrasena-de-aplicacion")
                && !normalizedValue.contains("contraseña-de-aplicacion");
    }

    private void logDevCode(String correo, String codigo) {
        System.out.printf(
                "Correo SMTP no configurado con credenciales reales. Codigo de recuperacion Musicplay para %s: %s%n",
                correo,
                codigo
        );
    }

    private record RecoveryCode(String code, Instant expiresAt) {
    }
}
