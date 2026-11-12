package cl.prosetup.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
        @Min(value = 0, message = "El stock no puede ser negativo") int stock,
        @Min(value = 0, message = "El stock crítico no puede ser negativo") int stockCritico,
        String imagen,
        boolean oferta,
        @Min(value = 0, message = "El descuento mínimo es 0") @Max(value = 100, message = "El descuento máximo es 100") int descuento,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId) {
}
