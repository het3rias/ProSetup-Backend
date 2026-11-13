package cl.prosetup.backend.controller;

import cl.prosetup.backend.dto.*;
import cl.prosetup.backend.service.AuthService;
import cl.prosetup.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login, renovación de token y usuario actual.")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Registrar un cliente nuevo (público). El rol siempre es CLIENTE")
    public UsuarioResponse registrar(@Valid @RequestBody RegisterRequest r) {
        return usuarios.registrar(r);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión: devuelve accessToken y refreshToken (público)")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar tokens usando el refreshToken (público)")
    public AuthResponse refrescar(@Valid @RequestBody RefreshRequest r) {
        return auth.refrescar(r.refreshToken());
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado (sirve para restaurar la sesión al recargar la página)")
    public UsuarioResponse yo(Authentication authentication) {
        return usuarios.perfil(authentication.getName());
    }
}
