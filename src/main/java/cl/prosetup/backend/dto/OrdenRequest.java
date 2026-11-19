package cl.prosetup.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Pedido de compra. El frontend solo envía qué productos y cuántas unidades:
 * los precios, el subtotal, el envío y el total los calcula el servidor.
 */
public record OrdenRequest(
        @NotNull(message = "Los datos del cliente son obligatorios") @Valid Cliente cliente,
        @Valid Direccion direccion,
        @NotBlank(message = "El tipo de entrega es obligatorio") String entrega,
        @NotEmpty(message = "La orden debe tener al menos un producto") @Valid List<ItemRequest> items,
        boolean simularRechazo) {

    public record Cliente(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotBlank(message = "Los apellidos son obligatorios") String apellidos,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String email) {
    }

    public record Direccion(String calle, String departamento, String region, String comuna, String indicaciones) {
    }

    public record ItemRequest(
            @NotNull(message = "El producto es obligatorio") Long id,
            @Min(value = 1, message = "La cantidad mínima es 1") int cantidad) {
    }
}
