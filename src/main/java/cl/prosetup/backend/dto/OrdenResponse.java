package cl.prosetup.backend.dto;

import cl.prosetup.backend.model.Orden;

import java.time.LocalDateTime;
import java.util.List;

/** Orden/boleta tal como la espera el frontend. */
public record OrdenResponse(
        Long id, String codigo, LocalDateTime fecha, String estado, String motivo, String usuarioEmail,
        ClienteDto cliente, DireccionDto direccion, String entrega, List<ItemDto> items,
        int subtotal, int costoEnvio, int total) {

    public record ClienteDto(String nombre, String apellidos, String email) {
    }

    public record DireccionDto(String calle, String departamento, String region, String comuna, String indicaciones) {
    }

    public record ItemDto(Long id, String codigo, String nombre, int precio, int cantidad, String imagen) {
    }

    /** Debe llamarse dentro de una transacción (usuario e items se cargan de forma perezosa). */
    public static OrdenResponse de(Orden o) {
        List<ItemDto> items = o.getItems().stream()
                .map(i -> new ItemDto(i.getProducto().getId(), i.getCodigo(), i.getNombre(),
                        i.getPrecioUnitario(), i.getCantidad(), i.getImagen()))
                .toList();
        return new OrdenResponse(o.getId(), "ORDER-" + o.getId(), o.getFecha(), o.getEstado().etiqueta(),
                o.getMotivo() == null ? "" : o.getMotivo(),
                o.getUsuario() == null ? null : o.getUsuario().getCorreo(),
                new ClienteDto(o.getClienteNombre(), o.getClienteApellidos(), o.getClienteEmail()),
                new DireccionDto(o.getCalle(), o.getDepartamento(), o.getRegion(), o.getComuna(), o.getIndicaciones()),
                o.getEntrega(), items, o.getSubtotal(), o.getCostoEnvio(), o.getTotal());
    }
}
