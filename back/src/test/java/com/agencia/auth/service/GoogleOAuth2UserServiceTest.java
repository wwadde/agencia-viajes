package com.agencia.auth.service;

import com.agencia.auth.entity.Rol;
import com.agencia.auth.entity.Usuario;
import com.agencia.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleUserProvisioningServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GoogleUserProvisioningService googleUserProvisioningService;

    @Test
    void obtenerOCrearUsuarioPorCorreo_debeRetornarExistenteSiYaExiste() {
        Usuario existente = Usuario.builder()
                .id(7L)
                .usuario("cliente1")
                .correo("cliente@correo.com")
                .password("hash")
                .rol(Rol.CLIENTE)
                .build();

        when(usuarioRepository.findByCorreoIgnoreCase("cliente@correo.com")).thenReturn(Optional.of(existente));

        Usuario resultado = googleUserProvisioningService.obtenerOCrearUsuarioPorCorreo("CLIENTE@CORREO.COM");

        assertEquals(7L, resultado.getId());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void obtenerOCrearUsuarioPorCorreo_debeCrearNuevoUsuarioConRolCliente() {
        when(usuarioRepository.findByCorreoIgnoreCase("nuevo.usuario@gmail.com")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByUsuario("nuevo.usuario")).thenReturn(true);
        when(usuarioRepository.existsByUsuario("nuevo.usuario2")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash-aleatorio");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(99L);
            return usuario;
        });

        Usuario creado = googleUserProvisioningService.obtenerOCrearUsuarioPorCorreo("nuevo.usuario@gmail.com");

        assertNotNull(creado.getId());
        assertEquals("nuevo.usuario2", creado.getUsername());
        assertEquals("nuevo.usuario@gmail.com", creado.getCorreo());
        assertEquals(Rol.CLIENTE, creado.getRol());
        assertEquals("hash-aleatorio", creado.getPassword());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertTrue(captor.getValue().getPassword().startsWith("hash"));
        verify(usuarioRepository).findByCorreoIgnoreCase("nuevo.usuario@gmail.com");
    }

    @Test
    void obtenerOCrearUsuarioDesdeGoogle_debeUsarGivenNameYFamilyNameComoBaseUsername() {
        Map<String, Object> attributes = Map.of(
                "email", "john.doe@gmail.com",
                "email_verified", true,
                "given_name", "John",
                "family_name", "Doe"
        );

        when(usuarioRepository.findByCorreoIgnoreCase("john.doe@gmail.com")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByUsuario("john.doe")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash-aleatorio");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(100L);
            return usuario;
        });

        Usuario creado = googleUserProvisioningService.obtenerOCrearUsuarioDesdeGoogle(attributes);

        assertEquals("john.doe", creado.getUsername());
        assertEquals("john.doe@gmail.com", creado.getCorreo());
        assertEquals(Rol.CLIENTE, creado.getRol());
    }

    @Test
    void obtenerOCrearUsuarioDesdeGoogle_debeFallarSiCorreoNoVerificado() {
        Map<String, Object> attributes = Map.of(
                "email", "john.doe@gmail.com",
                "email_verified", false
        );

        assertThrows(OAuth2AuthenticationException.class,
                () -> googleUserProvisioningService.obtenerOCrearUsuarioDesdeGoogle(attributes));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void obtenerOCrearUsuarioDesdeGoogle_debeFallarSiGoogleNoEnviaEstadoDeVerificacion() {
        Map<String, Object> attributes = Map.of(
                "email", "john.doe@gmail.com"
        );

        assertThrows(OAuth2AuthenticationException.class,
                () -> googleUserProvisioningService.obtenerOCrearUsuarioDesdeGoogle(attributes));
        verify(usuarioRepository, never()).save(any());
    }
}


