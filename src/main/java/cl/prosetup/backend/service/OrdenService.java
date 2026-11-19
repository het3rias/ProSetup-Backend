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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdenService {

    public static final int COSTO_ENVIO = 3990;
    public static final int ENVIO_GRATIS_DESDE = 50000;

    private final OrdenRepository ordenes;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public OrdenService(OrdenRepository ordenes, ProductoRepository productos, UsuarioRepository usuarios) {
        this.ordenes = ordenes;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /** Retiro en tienda: gratis. Despacho a domicilio: gratis desde $50.000, si no $3.990. */
    public static int calcularEnvio(int subtotal, String entrega) {
        if (!"domicilio".equals(entrega) || subtotal == 0) return 0;
        return subtotal >= ENVIO_GRATIS_DESDE ? 0 : COSTO_ENVIO;
    }

    /**
     * Lógica de negocio de la compra:
     * 1) junta las líneas repetidas y verifica que cada producto exista y tenga stock suficiente,
     * 2) calcula precios (con oferta), subtotal, envío y total en el servidor (no se confía en el frontend),
     * 3) si todo está bien, descuenta el stock y guarda la orden como "Pagada";
     *    si falta stock o el pago se rechaza, guarda la orden como "Rechazada" con su motivo y no toca el stock.
     * Puede comprar un usuario con sesión (correoUsuario) o un invitado (null).
     */
    @Transactional
    public OrdenResponse crear(String correoUsuario, OrdenRequest r) {
        String entrega = r.entrega();
        if (!"domicilio".equals(entrega) && !"retiro".equals(entrega)) {
            throw new BadRequestException("La entrega debe ser 'domicilio' o 'retiro'");
        }
        if ("domicilio".equals(entrega)
                && (r.direccion() == null || r.direccion().calle() == null || r.direccion().calle().isBlank())) {
            throw new BadRequestException("La dirección de entrega es obligatoria para despacho a domicilio");
        }

        Orden orden = new Orden();
        if (correoUsuario != null) {
            orden.setUsuario(usuarios.findByCorreoIgnoreCase(correoUsuario).orElse(null));
        }
        orden.setClienteNombre(r.cliente().nombre().trim());
        orden.setClienteApellidos(r.cliente().apellidos().trim());
        orden.setClienteEmail(r.cliente().email().trim().toLowerCase());
        orden.setEntrega(entrega);
        OrdenRequest.Direccion d = r.direccion();
        if ("domicilio".equals(entrega)) {
            orden.setCalle(d.calle().trim());
            orden.setDepartamento(d.departamento());
            orden.setRegion(d.region());
            orden.setComuna(d.comuna());
        } else {
            orden.setCalle("Retiro en tienda");
        }
        orden.setIndicaciones(d == null ? null : d.indicaciones());

        // Se juntan las líneas del mismo producto para validar el stock sobre el total pedido
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (OrdenRequest.ItemRequest it : r.items()) {
            cantidades.merge(it.id(), it.cantidad(), Integer::sum);
        }

        List<String> problemas = new ArrayList<>();
        List<Producto> comprados = new ArrayList<>();
        int subtotal = 0;
        for (Map.Entry<Long, Integer> e : cantidades.entrySet()) {
            Producto p = productos.buscarParaActualizar(e.getKey())
                    .orElseThrow(() -> new NotFoundException("No existe el producto con id " + e.getKey()));
            int cantidad = e.getValue();
            if (p.getStock() < cantidad) {
                problemas.add("Stock insuficiente de " + p.getNombre() + " (disponible: " + p.getStock() + ").");
            }
            OrdenItem item = new OrdenItem();
            item.setProducto(p);
            item.setCodigo(p.getCodigo());
            item.setNombre(p.getNombre());
            item.setImagen(p.getImagen());
            item.setPrecioUnitario(p.precioFinal());
            item.setCantidad(cantidad);
            orden.agregarItem(item);
            comprados.add(p);
            subtotal += item.subtotal();
        }

        int envio = calcularEnvio(subtotal, entrega);
        orden.setSubtotal(subtotal);
        orden.setCostoEnvio(envio);
        orden.setTotal(subtotal + envio);

        if (!problemas.isEmpty()) {
            orden.setEstado(EstadoOrden.RECHAZADA);
            orden.setMotivo("Stock insuficiente: " + String.join(" ", problemas));
        } else if (r.simularRechazo()) {
            orden.setEstado(EstadoOrden.RECHAZADA);
            orden.setMotivo("El medio de pago rechazó la transacción.");
        } else {
            orden.setEstado(EstadoOrden.PAGADA);
            for (Producto p : comprados) {
                p.setStock(p.getStock() - cantidades.get(p.getId()));
            }
        }
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

    /** Un Cliente solo puede ver sus propias órdenes; Administrador y Vendedor pueden ver todas. */
    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id, String correo, boolean esPersonal) {
        Orden o = ordenes.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la orden con id " + id));
        boolean esDueno = o.getUsuario() != null && o.getUsuario().getCorreo().equalsIgnoreCase(correo);
        if (!esPersonal && !esDueno) {
            throw new AccessDeniedException("No puedes ver órdenes de otros usuarios");
        }
        return OrdenResponse.de(o);
    }

    /** Cambia el estado de una orden pagada (Pagada, Enviada, Entregada). Las rechazadas no cambian. */
    @Transactional
    public OrdenResponse cambiarEstado(Long id, String estadoTexto) {
        EstadoOrden nuevo;
        try {
            nuevo = EstadoOrden.desdeEtiqueta(estadoTexto);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("El estado debe ser Pagada, Enviada o Entregada");
        }
        if (nuevo == EstadoOrden.RECHAZADA) {
            throw new BadRequestException("Una orden no se puede marcar como rechazada manualmente");
        }
        Orden o = ordenes.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la orden con id " + id));
        if (o.getEstado() == EstadoOrden.RECHAZADA) {
            throw new BadRequestException("La orden fue rechazada y no puede cambiar de estado");
        }
        o.setEstado(nuevo);
        return OrdenResponse.de(o);
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        long ventas = ordenes.findAll().stream()
                .filter(o -> o.getEstado() != EstadoOrden.RECHAZADA)
                .mapToLong(Orden::getTotal).sum();
        long totalOrdenes = ordenes.count();
        long criticos = productos.findAll().stream().filter(Producto::stockEsCritico).count();
        return new DashboardResponse(productos.count(), usuarios.count(), totalOrdenes, ventas, criticos);
    }
}
