package com.agencia.controller.publico;

import com.agencia.domain.dto.LoginDTO;
import com.agencia.domain.dto.RegistroUsuarioDTO;
import com.agencia.domain.dto.UserInfoDTO;
import com.agencia.domain.Usuario;
import com.agencia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controlador de autenticación.
 *
 * <p>Expone los endpoints de login, logout, registro y consulta del usuario actual.
 * La autenticación se gestiona mediante cookies de sesión en memoria del servidor (HttpSession),
 * no mediante tokens JWT en el header Authorization.
 *
 * <h3>Flujo típico desde Angular:</h3>
 * <ol>
 *   <li>POST /api/auth/login → recibe cookie SESSION + JSON con info del usuario.</li>
 *   <li>Todas las peticiones siguientes incluyen la cookie SESSION automáticamente.</li>
 *   <li>POST /api/auth/logout → invalida la sesión y borra la cookie.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Login, logout, registro y perfil del usuario")
public class AuthController {

    private final AuthService authService;

    /**
     * Autentica al usuario con sus credenciales y crea una sesión HTTP.
     * La cookie SESSION se establece automáticamente en la respuesta.
     *
     * @param loginDTO credenciales del usuario
     * @param request  necesario para crear la sesión
     * @param response necesario para escribir la cookie
     * @return 200 OK con la información del usuario autenticado
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica las credenciales y crea una sesión HTTP. La cookie SESSION se envía en la respuesta.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso"),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
            }
    )
    public ResponseEntity<UserInfoDTO> login(
            @RequestBody @Valid LoginDTO loginDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        UserInfoDTO userInfo = authService.login(loginDTO, request, response);
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }


    /**
     * Registra un nuevo usuario en el sistema con rol CLIENTE.
     *
     * @param dto datos del nuevo usuario
     * @return 201 Created con la URI del recurso creado
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            description = "Crea una nueva cuenta de usuario con rol CLIENTE.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado"),
                    @ApiResponse(responseCode = "409", description = "El nombre de usuario o correo ya existe"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
            }
    )
    public ResponseEntity<Void> register(@RequestBody @Valid RegistroUsuarioDTO dto) {
        Long usuarioId = authService.register(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuarioId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * Devuelve la información del usuario actualmente autenticado.
     * Útil para que el frontend Angular inicialice el estado de la sesión al cargar la aplicación.
     *
     * @param usuario usuario autenticado inyectado por Spring Security
     * @return 200 OK con la información del usuario
     */
    @GetMapping("/me")
    @Operation(
            summary = "Usuario actual",
            description = "Devuelve la información del usuario autenticado en la sesión activa.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Información del usuario"),
                    @ApiResponse(responseCode = "401", description = "No autenticado")
            }
    )
    public ResponseEntity<UserInfoDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new UserInfoDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getCorreo(),
                usuario.getRol()
        ));
    }

    @GetMapping("/csrf")
    public void obtenerCsrf(){
        // Este endpoint es solo para que el frontend Angular obtenga el token CSRF necesario para las peticiones
    }
}
