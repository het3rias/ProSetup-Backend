package cl.prosetup.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.prosetup.backend.dto.ProductoRequest;
import cl.prosetup.backend.dto.ProductoResponse;
import cl.prosetup.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "CRUD de productos. Lectura pública; crear, editar y eliminar solo ADMIN.")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos (público). Filtros opcionales: q, categoriaId, oferta")
    public List<ProductoResponse> listar(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) Long categoriaId,
                                 @RequestParam(required = false) Boolean oferta) {
        return service.listar(q, categoriaId, oferta);
    }

    @GetMapping("/criticos")
    @Operation(summary = "Productos con stock crítico (ADMIN)")
    public List<ProductoResponse> criticos() {
        return service.criticos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por id (público)")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear producto (ADMIN)")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest r) {
        return service.crear(r);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (ADMIN)")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar producto (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
