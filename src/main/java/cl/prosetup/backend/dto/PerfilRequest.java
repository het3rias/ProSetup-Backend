package cl.prosetup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Datos que un usuario puede cambiar de su propio perfil (no puede cambiar tipo, correo ni RUN). */
public record PerfilRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 120) String apellidos,
        @Size(max = 15) String telefono,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        @Size(max = 200) String direccion,
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres") String password) {
}
