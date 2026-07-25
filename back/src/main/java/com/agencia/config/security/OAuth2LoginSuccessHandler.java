package com.agencia.config.security;

import com.agencia.domain.Usuario;
import com.agencia.service.GoogleUserProvisioningService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleUserProvisioningService googleUserProvisioningService;
    private final SecurityContextRepository securityContextRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.auth.google.success-path}")
    private String successPath;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new ServletException("Principal OAuth2 invalido");
        }

        Usuario usuario = googleUserProvisioningService.obtenerOCrearUsuarioDesdeGoogle(oauth2User.getAttributes());
        UsernamePasswordAuthenticationToken sessionAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                usuario,
                null,
                usuario.getAuthorities()
        );
        sessionAuthentication.setDetails(authentication.getDetails());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(sessionAuthentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path(successPath)
                .queryParam("status", "ok")
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}

