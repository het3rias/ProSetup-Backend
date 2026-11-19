package cl.prosetup.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar un producto. La categoría se indica por su nombre, igual que en el frontend. */
public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 30, message = "El código admite hasta 30 caracteres")
        String codigo,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre admite hasta 150 caracteres")
        String nombre,
        @Size(max = 1000, message = "La descripción admite hasta 1000 caracteres")
        String descripcion,
        @Min(value = 0, message = "El precio no puede ser negativo") int precio,
        @Min(value = 0, message = "El precio de oferta no puede ser negativo") Integer precioOferta,
        @Min(value = 0, message = "El stock no puede ser negativo") int stock,
        @Min(value = 0, message = "El stock crítico no puede ser negativo") Integer stockCritico,
        @NotBlank(message = "La categoría es obligatoria") String categoria,
        @Size(max = 500, message = "La imagen admite hasta 500 caracteres") String imagen) {
}
