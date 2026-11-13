package cl.prosetup.backend.controller;

import cl.prosetup.backend.dto.CategoriaRequest;
import cl.prosetup.backend.model.Categoria;
import cl.prosetup.backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "CRUD de categorías. Lectura pública; escritura solo ADMIN.")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar categorías (público)")
    public List<Categoria> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por id (público)")
    public Categoria obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría (ADMIN)")
    public Categoria crear(@Valid @RequestBody CategoriaRequest r) {
        return service.crear(r);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría (ADMIN)")
    public Categoria actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar categoría (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
