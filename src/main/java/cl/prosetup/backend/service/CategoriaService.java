package cl.prosetup.backend.service;

import cl.prosetup.backend.dto.CategoriaRequest;
import cl.prosetup.backend.exception.ConflictException;
import cl.prosetup.backend.exception.NotFoundException;
import cl.prosetup.backend.model.Categoria;
import cl.prosetup.backend.repository.CategoriaRepository;
import cl.prosetup.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categorias.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la categoría con id " + id));
    }

    @Transactional
    public Categoria crear(CategoriaRequest r) {
        if (categorias.existsByNombreIgnoreCase(r.nombre().trim())) {
            throw new ConflictException("Ya existe una categoría llamada '" + r.nombre().trim() + "'");
        }
        return categorias.save(new Categoria(r.nombre().trim(), r.descripcion()));
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaRequest r) {
        Categoria c = obtener(id);
        if (!c.getNombre().equalsIgnoreCase(r.nombre().trim())
                && categorias.existsByNombreIgnoreCase(r.nombre().trim())) {
            throw new ConflictException("Ya existe una categoría llamada '" + r.nombre().trim() + "'");
        }
        c.setNombre(r.nombre().trim());
        c.setDescripcion(r.descripcion());
        return categorias.save(c);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = obtener(id);
        if (productos.existsByCategoriaId(id)) {
            throw new ConflictException("No se puede eliminar la categoría porque tiene productos asociados");
        }
        categorias.delete(c);
    }
}
