package cl.prosetup.backend.dto;

import cl.prosetup.backend.model.EstadoOrden;
import cl.prosetup.backend.model.Orden;

import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long id, LocalDateTime fecha, EstadoOrden estado, int total,
        String direccionEnvio, String region, String comuna,
        Long usuarioId, String usuarioNombre, String usuarioCorreo,
        List<ItemResponse> items) {

    public record ItemResponse(Long productoId, String nombre, int precioUnitario, int cantidad, int subtotal) {
    }

    /** Debe llamarse dentro de una transacción (usuario e items se cargan de forma perezosa). */
    public static OrdenResponse de(Orden o) {
        List<ItemResponse> items = o.getItems().stream()
                .map(i -> new ItemResponse(i.getProducto().getId(), i.getNombreProducto(),
                        i.getPrecioUnitario(), i.getCantidad(), i.getSubtotal()))
                .toList();
        return new OrdenResponse(o.getId(), o.getFecha(), o.getEstado(), o.getTotal(),
                o.getDireccionEnvio(), o.getRegion(), o.getComuna(),
                o.getUsuario().getId(),
                o.getUsuario().getNombre() + " " + o.getUsuario().getApellidos(),
                o.getUsuario().getCorreo(), items);
    }
}
