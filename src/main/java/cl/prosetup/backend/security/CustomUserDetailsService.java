package cl.prosetup.backend.security;

import cl.prosetup.backend.model.Usuario;
import cl.prosetup.backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public CustomUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario u = usuarios.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        return User.withUsername(u.getCorreo())
                .password(u.getPassword())
                .roles(u.getRol().name()) // Spring agrega el prefijo ROLE_ (ROLE_ADMIN, ROLE_VENDEDOR, ROLE_CLIENTE)
                .disabled(!u.isActivo())
                .build();
    }
}
