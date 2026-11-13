package cl.prosetup.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrdenRequest(
        @NotEmpty(message = "La orden debe tener al menos un producto") @Valid List<ItemRequest> items,
        String direccionEnvio,
        String region,
        String comuna) {

    public record ItemRequest(
            @NotNull(message = "El producto es obligatorio") Long productoId,
            @Min(value = 1, message = "La cantidad mínima es 1") int cantidad) {
    }
}
