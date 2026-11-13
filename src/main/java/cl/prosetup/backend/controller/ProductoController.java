package cl.prosetup.backend.controller;

import cl.prosetup.backend.dto.ProductoRequest;
import cl.prosetup.backend.model.Producto;
import cl.prosetup.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "CRUD de productos. Lectura pública; escritura ADMIN/VENDEDOR; eliminar solo ADMIN.")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos (público). Filtros opcionales: q, categoriaId, oferta")
    public List<Producto> listar(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) Long categoriaId,
                                 @RequestParam(required = false) Boolean oferta) {
        return service.listar(q, categoriaId, oferta);
    }

    @GetMapping("/criticos")
    @Operation(summary = "Productos con stock crítico (ADMIN/VENDEDOR)")
    public List<Producto> criticos() {
        return service.criticos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por id (público)")
    public Producto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear producto (ADMIN/VENDEDOR)")
    public Producto crear(@Valid @RequestBody ProductoRequest r) {
        return service.crear(r);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (ADMIN/VENDEDOR)")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar producto (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
