package cl.prosetup.backend.dto;

import cl.prosetup.backend.model.Usuario;

import java.time.LocalDate;

/** Usuario tal como lo espera el frontend. Nunca incluye la contraseña. */
public record UsuarioResponse(
        Long id, String run, String nombre, String apellidos, String email, String telefono,
        LocalDate fechaNacimiento, String tipoUsuario, String region, String comuna, String direccion,
        boolean activo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getTelefono(), u.getFechaNacimiento(), u.getRol().etiqueta(), u.getRegion(),
                u.getComuna(), u.getDireccion(), u.isActivo());
    }
}
