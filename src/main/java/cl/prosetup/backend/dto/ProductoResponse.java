package cl.prosetup.backend.dto;

import cl.prosetup.backend.model.Producto;

/** Producto tal como lo espera el frontend (la categoría va como nombre). */
public record ProductoResponse(
        Long id, String codigo, String nombre, String descripcion,
        int precio, Integer precioOferta, int stock, Integer stockCritico,
        String categoria, String imagen) {

    public static ProductoResponse de(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getPrecioOferta(), p.getStock(), p.getStockCritico(),
                p.getCategoria().getNombre(), p.getImagen());
    }
}
