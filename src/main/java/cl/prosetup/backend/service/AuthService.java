package cl.prosetup.backend.service;

import cl.prosetup.backend.dto.AuthResponse;
import cl.prosetup.backend.dto.LoginRequest;
import cl.prosetup.backend.dto.UsuarioResponse;
import cl.prosetup.backend.model.Usuario;
import cl.prosetup.backend.repository.UsuarioRepository;
import cl.prosetup.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarios;
    private final JwtService jwt;

    public AuthService(AuthenticationManager authManager, UsuarioRepository usuarios, JwtService jwt) {
        this.authManager = authManager;
        this.usuarios = usuarios;
        this.jwt = jwt;
    }

    /** Valida correo y contraseña (BCrypt) y entrega access token + refresh token. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(r.correo().trim(), r.password()));
        Usuario u = usuarios.findByCorreoIgnoreCase(r.correo().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        return respuesta(u);
    }

    /** Renueva la sesión: con un refresh token válido se emite un nuevo par de tokens. */
    @Transactional(readOnly = true)
    public AuthResponse refrescar(String refreshToken) {
        try {
            Claims claims = jwt.parsear(refreshToken);
            if (!jwt.esTipo(claims, JwtService.TIPO_REFRESH)) {
                throw new BadCredentialsException("El token no es un refresh token");
            }
            Usuario u = usuarios.findByCorreoIgnoreCase(claims.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
            if (!u.isActivo()) {
                throw new BadCredentialsException("Usuario desactivado");
            }
            return respuesta(u);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Refresh token inválido o vencido");
        }
    }

    private AuthResponse respuesta(Usuario u) {
        return new AuthResponse(jwt.generarAccessToken(u), jwt.generarRefreshToken(u), "Bearer",
                jwt.segundosAccessToken(), UsuarioResponse.de(u));
    }
}
