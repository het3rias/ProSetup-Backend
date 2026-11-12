package cl.prosetup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
        String nombre,
        @Size(max = 300, message = "La descripción admite hasta 300 caracteres")
        String descripcion) {
}
