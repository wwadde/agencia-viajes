package com.agencia.auth.service;

import com.agencia.auth.dto.LoginDTO;
import com.agencia.auth.dto.RegistroUsuarioDTO;
import com.agencia.auth.dto.UserInfoDTO;
import com.agencia.auth.entity.Rol;
import com.agencia.auth.entity.Usuario;
import com.agencia.auth.exception.UsuarioYaExisteException;
import com.agencia.auth.mapper.UsuarioMapper;
import com.agencia.auth.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación: login, logout y registro de usuarios.
 *
 * <h3>Flujo de login</h3>
 * <ol>
 *   <li>Se validan las credenciales con {@link AuthenticationManager}.</li>
 *   <li>Se crea un nuevo {@link SecurityContext} con la autenticación establecida.</li>
 *   <li>{@link HttpSessionSecurityContextRepository} persiste ese contexto en la
 *       {@code HttpSession} del servidor (en memoria JVM), lo que crea automáticamente
 *       la cookie {@code SESSION} en la respuesta.</li>
 * </ol>
 *
 * <p>El logout se gestiona en el controlador invalidando directamente la sesión.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    /**
     * Autentica al usuario y abre una sesión HTTP.
     *
     * @param loginDTO credenciales
     * @param request  petición HTTP (se usa para crear/obtener la sesión)
     * @param response respuesta HTTP (se usa para escribir la cookie SESSION)
     * @return información básica del usuario autenticado
     */
    public UserInfoDTO login(LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.usuario(), loginDTO.password())
        );

        // Nuevo contexto vacío + autenticación → protección contra Session Fixation
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Guardar el contexto en la HttpSession; esto crea la sesión y emite la cookie SESSION
        securityContextRepository.saveContext(context, request, response);

        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new BadCredentialsException("Principal autenticado invalido");
        }
        return usuarioMapper.toUserInfoDTO(usuario);
    }



    /**
     * Registra un nuevo usuario con rol {@link Rol#CLIENTE}.
     *
     * @param dto datos del nuevo usuario
     * @return ID del usuario creado
     * @throws UsuarioYaExisteException si el nombre de usuario o correo ya está en uso
     */
    @Transactional
    public Long register(RegistroUsuarioDTO dto) {
        if (usuarioRepository.existsByUsuario(dto.usuario())) {
            throw new UsuarioYaExisteException(
                    "El nombre de usuario '%s' ya está en uso".formatted(dto.usuario()));
        }
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new UsuarioYaExisteException(
                    "El correo '%s' ya está registrado".formatted(dto.correo()));
        }

        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        usuario.setRol(Rol.CLIENTE);

        Long id = usuarioRepository.save(usuario).getId();
        log.info("Nuevo usuario registrado con id: {}", id);
        return id;
    }
}
