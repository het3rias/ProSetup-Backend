package cl.prosetup.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Nuevo estado de una orden: "Pagada", "Enviada" o "Entregada". */
public record EstadoRequest(@NotBlank(message = "El estado es obligatorio") String estado) {
}
