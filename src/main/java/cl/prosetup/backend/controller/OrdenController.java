package cl.prosetup.backend.controller;

import cl.prosetup.backend.dto.EstadoRequest;
import cl.prosetup.backend.dto.OrdenRequest;
import cl.prosetup.backend.dto.OrdenResponse;
import cl.prosetup.backend.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Compras. Cualquier usuario autenticado compra; ADMIN/VENDEDOR gestionan.")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una orden de compra (usuario autenticado)")
    public OrdenResponse crear(Authentication auth, @Valid @RequestBody OrdenRequest r) {
        return service.crear(auth.getName(), r);
    }

    @GetMapping("/mias")
    @Operation(summary = "Listar mis órdenes (usuario autenticado)")
    public List<OrdenResponse> mias(Authentication auth) {
        return service.listarMias(auth.getName());
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes (ADMIN/VENDEDOR)")
    public List<OrdenResponse> todas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una orden (dueño o ADMIN/VENDEDOR)")
    public OrdenResponse obtener(@PathVariable Long id, Authentication auth) {
        return service.obtener(id, auth.getName(), esPersonal(auth));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una orden (ADMIN/VENDEDOR)")
    public OrdenResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest r) {
        return service.cambiarEstado(id, r.estado());
    }

    private boolean esPersonal(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"));
    }
}
