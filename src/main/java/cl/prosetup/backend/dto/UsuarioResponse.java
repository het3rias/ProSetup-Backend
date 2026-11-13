package cl.prosetup.backend.dto;

import cl.prosetup.backend.model.Role;
import cl.prosetup.backend.model.Usuario;

import java.time.LocalDate;

/** Representación pública de un usuario: nunca incluye la contraseña. */
public record UsuarioResponse(
        Long id, String run, String nombre, String apellidos, String correo,
        LocalDate fechaNacimiento, String region, String comuna, String direccion,
        Role rol, boolean activo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getFechaNacimiento(), u.getRegion(), u.getComuna(), u.getDireccion(),
                u.getRol(), u.isActivo());
    }
}
