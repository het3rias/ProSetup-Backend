package cl.prosetup.backend.service;

import cl.prosetup.backend.dto.ProductoRequest;
import cl.prosetup.backend.dto.ProductoResponse;
import cl.prosetup.backend.exception.BadRequestException;
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
    public List<ProductoResponse> listar(String q, Long categoriaId, Boolean oferta) {
        return productos.findAll().stream()
                .filter(p -> q == null || q.isBlank()
                        || p.getNombre().toLowerCase().contains(q.trim().toLowerCase()))
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> oferta == null || (p.precioFinal() < p.getPrecio()) == oferta)
                .map(ProductoResponse::de)
                .toList();
    }

    /** Productos con stock igual o menor al stock crítico. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> criticos() {
        return productos.findAll().stream()
                .filter(Producto::stockEsCritico)
                .map(ProductoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.de(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest r) {
        if (productos.existsByCodigoIgnoreCase(r.codigo().trim())) {
            throw new ConflictException("Ya existe un producto con el código " + r.codigo().trim());
        }
        Producto p = new Producto();
        aplicar(p, r);
        return ProductoResponse.de(productos.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest r) {
        Producto p = buscar(id);
        if (!p.getCodigo().equalsIgnoreCase(r.codigo().trim())
                && productos.existsByCodigoIgnoreCase(r.codigo().trim())) {
            throw new ConflictException("Ya existe un producto con el código " + r.codigo().trim());
        }
        aplicar(p, r);
        return ProductoResponse.de(productos.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        productos.delete(p);
        productos.flush(); // si el producto ya está en una orden, falla aquí y responde 409
    }

    private Producto buscar(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el producto con id " + id));
    }

    private void aplicar(Producto p, ProductoRequest r) {
        Categoria c = categorias.findByNombreIgnoreCase(r.categoria().trim())
                .orElseThrow(() -> new NotFoundException("No existe la categoría '" + r.categoria() + "'"));
        if (r.precioOferta() != null && r.precioOferta() >= r.precio()) {
            throw new BadRequestException("El precio de oferta debe ser menor al precio normal");
        }
        p.setCodigo(r.codigo().trim().toUpperCase());
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setPrecio(r.precio());
        p.setPrecioOferta(r.precioOferta());
        p.setStock(r.stock());
        p.setStockCritico(r.stockCritico());
        p.setImagen(r.imagen());
        p.setCategoria(c);
    }
}
