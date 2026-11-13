package cl.prosetup.backend.service;

import cl.prosetup.backend.dto.DashboardResponse;
import cl.prosetup.backend.dto.OrdenRequest;
import cl.prosetup.backend.dto.OrdenResponse;
import cl.prosetup.backend.exception.BadRequestException;
import cl.prosetup.backend.exception.NotFoundException;
import cl.prosetup.backend.model.*;
import cl.prosetup.backend.repository.OrdenRepository;
import cl.prosetup.backend.repository.ProductoRepository;
import cl.prosetup.backend.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenService {

    private final OrdenRepository ordenes;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public OrdenService(OrdenRepository ordenes, ProductoRepository productos, UsuarioRepository usuarios) {
        this.ordenes = ordenes;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /**
     * Lógica de negocio de la compra:
     * 1) verifica que cada producto exista y tenga stock suficiente,
     * 2) calcula el precio con descuento y el total en el servidor (no se confía en el frontend),
     * 3) descuenta el stock y guarda la orden con su detalle.
     */
    @Transactional
    public OrdenResponse crear(String correo, OrdenRequest r) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + correo));

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setEstado(EstadoOrden.PAGADA); // el pago es simulado
        orden.setDireccionEnvio(r.direccionEnvio() != null ? r.direccionEnvio() : usuario.getDireccion());
        orden.setRegion(r.region() != null ? r.region() : usuario.getRegion());
        orden.setComuna(r.comuna() != null ? r.comuna() : usuario.getComuna());

        int total = 0;
        for (OrdenRequest.ItemRequest it : r.items()) {
            Producto p = productos.findById(it.productoId())
                    .orElseThrow(() -> new NotFoundException("No existe el producto con id " + it.productoId()));
            if (p.getStock() < it.cantidad()) {
                throw new BadRequestException("Stock insuficiente para '" + p.getNombre()
                        + "'. Disponible: " + p.getStock());
            }
            p.setStock(p.getStock() - it.cantidad());

            OrdenItem item = new OrdenItem();
            item.setProducto(p);
            item.setNombreProducto(p.getNombre());
            item.setPrecioUnitario(p.getPrecioFinal());
            item.setCantidad(it.cantidad());
            item.setSubtotal(p.getPrecioFinal() * it.cantidad());
            orden.agregarItem(item);
            total += item.getSubtotal();
        }
        orden.setTotal(total);
        return OrdenResponse.de(ordenes.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenes.findAllByOrderByFechaDesc().stream().map(OrdenResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarMias(String correo) {
        return ordenes.findByUsuarioCorreoIgnoreCaseOrderByFechaDesc(correo).stream()
                .map(OrdenResponse::de).toList();
    }

    /** Un CLIENTE solo puede ver sus propias órdenes; ADMIN y VENDEDOR pueden ver todas. */
    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id, String correo, boolean esPersonal) {
        Orden o = ordenes.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la orden con id " + id));
        if (!esPersonal && !o.getUsuario().getCorreo().equalsIgnoreCase(correo)) {
            throw new AccessDeniedException("No puedes ver órdenes de otros usuarios");
        }
        return OrdenResponse.de(o);
    }

    /** Si se cancela una orden, se devuelve el stock de sus productos. */
    @Transactional
    public OrdenResponse cambiarEstado(Long id, EstadoOrden nuevo) {
        Orden o = ordenes.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la orden con id " + id));
        if (o.getEstado() == EstadoOrden.CANCELADA) {
            throw new BadRequestException("La orden ya está cancelada y no puede cambiar de estado");
        }
        if (nuevo == EstadoOrden.CANCELADA) {
            for (OrdenItem item : o.getItems()) {
                Producto p = item.getProducto();
                p.setStock(p.getStock() + item.getCantidad());
            }
        }
        o.setEstado(nuevo);
        return OrdenResponse.de(o);
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<Orden> todas = ordenes.findAll();
        long ventas = todas.stream()
                .filter(o -> o.getEstado() != EstadoOrden.CANCELADA)
                .mapToLong(Orden::getTotal).sum();
        long criticos = productos.findAll().stream()
                .filter(p -> p.getStock() <= p.getStockCritico()).count();
        return new DashboardResponse(productos.count(), usuarios.count(), todas.size(), ventas, criticos);
    }
}
