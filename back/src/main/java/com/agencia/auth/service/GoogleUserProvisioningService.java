package com.agencia.service;

import com.agencia.domain.Rol;
import com.agencia.domain.Usuario;
import com.agencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleUserProvisioningService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario obtenerOCrearUsuarioPorCorreo(String correo) {
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        return usuarioRepository.findByCorreoIgnoreCase(correoNormalizado)
                .orElseGet(() -> crearUsuarioDesdeGoogle(correoNormalizado, Map.of()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario obtenerOCrearUsuarioDesdeGoogle(Map<String, Object> attributes) {
        validarCorreoVerificado(attributes);
        String correo = obtenerCorreoRequerido(attributes);
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseGet(() -> crearUsuarioDesdeGoogle(correo, attributes));
    }

    private void validarCorreoVerificado(Map<String, Object> attributes) {
        Object emailVerified = attributes.get("email_verified");
        if (emailVerified == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request"),
                    "Google no devolvio el estado de verificacion del correo");
        }

        boolean verificado;
        if (emailVerified instanceof Boolean b) {
            verificado = b;
        } else if (emailVerified instanceof String s) {
            verificado = Boolean.parseBoolean(s);
        } else {
            verificado = false;
        }

        if (!verificado) {
            throw new OAuth2AuthenticationException(new OAuth2Error("access_denied"),
                    "El correo de Google no esta verificado");
        }
    }

    private String obtenerCorreoRequerido(Map<String, Object> attributes) {
        Object correo = attributes.get("email");
        if (!(correo instanceof String correoTexto) || correoTexto.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request"),
                    "Google no devolvio el correo del usuario");
        }
        return correoTexto.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario crearUsuarioDesdeGoogle(String correo, Map<String, Object> attributes) {
        String base = construirUsernameBase(attributes, correo);
        String candidato = base;
        int contador = 1;

        while (usuarioRepository.existsByUsuario(candidato)) {
            contador++;
            candidato = base + contador;
        }

        Usuario usuario = Usuario.builder()
                .usuario(candidato)
                .correo(correo)
                // Password aleatoria para mantener compatibilidad con esquema actual (NOT NULL).
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .rol(Rol.CLIENTE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado desde login Google. id={}, correo={}", guardado.getId(), guardado.getCorreo());
        return guardado;
    }

    private String construirUsernameBase(Map<String, Object> attributes, String correo) {
        String givenName = normalizarTexto((String) attributes.get("given_name"));
        String familyName = normalizarTexto((String) attributes.get("family_name"));
        String fullName = normalizarTexto((String) attributes.get("name"));

        if (!givenName.isBlank() || !familyName.isBlank()) {
            String compuesto = (givenName + "." + familyName).replaceAll("\\.+", ".");
            compuesto = compuesto.replaceAll("(^\\.)|(\\.$)", "");
            if (!compuesto.isBlank()) {
                return compuesto;
            }
        }

        if (!fullName.isBlank()) {
            String fromName = fullName.replace(' ', '.').replaceAll("\\.+", ".");
            fromName = fromName.replaceAll("(^\\.)|(\\.$)", "");
            if (!fromName.isBlank()) {
                return fromName;
            }
        }

        return slugUsuario(correo);
    }

    private String normalizarTexto(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9 ]", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String slugUsuario(String correo) {
        String localPart = correo.contains("@") ? correo.substring(0, correo.indexOf('@')) : correo;
        String normalizado = Normalizer.normalize(localPart, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase(Locale.ROOT);

        if (normalizado.isBlank()) {
            return "googleuser";
        }
        return normalizado;
    }
}

