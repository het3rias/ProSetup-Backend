package cl.prosetup.backend.config;

import cl.prosetup.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Seguridad de la API: sin sesiones (STATELESS), autenticación por JWT y autorización por rol.
 * Importante: Spring evalúa las reglas en orden, la primera que coincide gana.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // no hay cookies de sesión, se usa token
            .cors(cors -> {})                      // usa el bean corsConfigurationSource
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Documentación Swagger
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Autenticación pública
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh").permitAll()

                // Productos: ver es público; el stock crítico lo ven ADMIN y VENDEDOR; crear, editar y eliminar solo ADMIN
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/criticos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/productos", "/api/v1/productos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/productos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**").hasRole("ADMIN")

                // Categorías: ver es público, escribir solo ADMIN
                .requestMatchers(HttpMethod.GET, "/api/v1/categorias", "/api/v1/categorias/**").permitAll()
                .requestMatchers("/api/v1/categorias/**", "/api/v1/categorias").hasRole("ADMIN")

                // Usuarios: /me para cualquier autenticado, el resto solo ADMIN
                .requestMatchers("/api/v1/usuarios/me").authenticated()
                .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMIN")

                // Órdenes: se puede comprar sin iniciar sesión (invitado); ver y gestionar requiere rol
                .requestMatchers(HttpMethod.POST, "/api/v1/ordenes").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/mias").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/ordenes").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/ordenes/*/estado").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/*").authenticated()

                // Panel de administración
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Cualquier otra ruta (por ejemplo /api/auth/me) requiere estar autenticado
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> escribir(res, 401,
                        "No autenticado: falta el token o no es válido"))
                .accessDeniedHandler((req, res, ex) -> escribir(res, 403,
                        "Acceso denegado: tu rol no tiene permiso para este recurso")))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** Permite que el frontend (Vite en localhost:5173) llame a la API desde otro origen. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String origenes) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static void escribir(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}
