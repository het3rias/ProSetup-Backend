package cl.prosetup.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Registro público: el tipo de usuario siempre es Cliente (no se puede elegir desde afuera). */
public record RegisterRequest(
        @NotBlank(message = "El RUN es obligatorio") String run,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 120) String apellidos,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres") String password,
        @Size(max = 15) String telefono,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        @Size(max = 200) String direccion) {
}
