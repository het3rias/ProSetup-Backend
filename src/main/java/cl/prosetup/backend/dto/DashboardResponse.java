package cl.prosetup.backend.dto;

public record DashboardResponse(
        long totalProductos, long totalUsuarios, long totalOrdenes,
        long ventasTotales, long productosCriticos) {
}
