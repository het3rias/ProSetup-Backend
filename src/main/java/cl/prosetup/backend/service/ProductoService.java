package cl.prosetup.backend.service;

import cl.prosetup.backend.dto.ProductoRequest;
import cl.prosetup.backend.exception.ConflictException;
import cl.prosetup.backend.exception.NotFoundException;
import cl.prosetup.backend.model.Categoria;
import cl.prosetup.backend.model.Producto;
import cl.prosetup.backend.repository.CategoriaRepository;
import cl.prosetup.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    /** Lista con filtros opcionales: texto en el nombre, categoría y solo ofertas. */
    @Transactional(readOnly = true)
    public List<Producto> listar(String q, Long categoriaId, Boolean oferta) {
        return productos.findAll().stream()
                .filter(p -> q == null || q.isBlank()
                        || p.getNombre().toLowerCase().contains(q.trim().toLowerCase()))
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> oferta == null || p.isOferta() == oferta)
                .toList();
    }

    /** Productos con stock igual o menor al stock crítico. */
    @Transactional(readOnly = true)
    public List<Producto> criticos() {
        return productos.findAll().stream()
                .filter(p -> p.getStock() <= p.getStockCritico())
                .toList();
    }

    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el producto con id " + id));
    }

    @Transactional
    public Producto crear(ProductoRequest r) {
        if (productos.existsByCodigoIgnoreCase(r.codigo().trim())) {
            throw new ConflictException("Ya existe un producto con el código " + r.codigo().trim());
        }
        Producto p = new Producto();
        aplicar(p, r);
        return productos.save(p);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoRequest r) {
        Producto p = obtener(id);
        if (!p.getCodigo().equalsIgnoreCase(r.codigo().trim())
                && productos.existsByCodigoIgnoreCase(r.codigo().trim())) {
            throw new ConflictException("Ya existe un producto con el código " + r.codigo().trim());
        }
        aplicar(p, r);
        return productos.save(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = obtener(id);
        productos.delete(p);
        productos.flush(); // si el producto ya está en una orden, falla aquí y responde 409
    }

    private void aplicar(Producto p, ProductoRequest r) {
        Categoria c = categorias.findById(r.categoriaId())
                .orElseThrow(() -> new NotFoundException("No existe la categoría con id " + r.categoriaId()));
        p.setCodigo(r.codigo().trim());
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setPrecio(r.precio());
        p.setStock(r.stock());
        p.setStockCritico(r.stockCritico());
        p.setImagen(r.imagen());
        p.setOferta(r.oferta());
        p.setDescuento(r.oferta() ? r.descuento() : 0);
        p.setCategoria(c);
    }
}
