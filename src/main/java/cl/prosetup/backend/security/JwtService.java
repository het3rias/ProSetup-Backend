package cl.prosetup.backend.security;

import cl.prosetup.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los tokens JWT.
 * - Access token: dura poco (30 min por defecto) y se envía en cada petición.
 * - Refresh token: dura más (7 días) y solo sirve para pedir un nuevo access token.
 * Ambos van firmados con HMAC-SHA y llevan un claim "type" para que uno no se use por el otro.
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long accessMinutos;
    private final long refreshDias;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.access-minutes:30}") long accessMinutos,
                      @Value("${app.jwt.refresh-days:7}") long refreshDias) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.accessMinutos = accessMinutos;
        this.refreshDias = refreshDias;
    }

    public String generarAccessToken(Usuario u) {
        return construir(u, TIPO_ACCESS, accessMinutos * 60_000L);
    }

    public String generarRefreshToken(Usuario u) {
        return construir(u, TIPO_REFRESH, refreshDias * 24L * 60L * 60_000L);
    }

    /** Duración del access token en segundos (se informa al frontend). */
    public long segundosAccessToken() {
        return accessMinutos * 60L;
    }

    /** Verifica firma y expiración. Lanza JwtException si el token no es válido. */
    public Claims parsear(String token) throws JwtException {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    public boolean esTipo(Claims claims, String tipo) {
        return tipo.equals(claims.get("type", String.class));
    }

    private String construir(Usuario u, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(u.getCorreo())
                .claim("rol", u.getRol().name())
                .claim("type", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }
}
