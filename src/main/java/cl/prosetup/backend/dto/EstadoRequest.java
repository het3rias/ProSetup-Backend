package cl.prosetup.backend.dto;

import cl.prosetup.backend.model.EstadoOrden;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull(message = "El estado es obligatorio") EstadoOrden estado) {
}
