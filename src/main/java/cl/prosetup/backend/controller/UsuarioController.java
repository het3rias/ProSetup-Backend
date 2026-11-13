package cl.prosetup.backend.controller;

import cl.prosetup.backend.dto.PerfilRequest;
import cl.prosetup.backend.dto.UsuarioRequest;
import cl.prosetup.backend.dto.UsuarioResponse;
import cl.prosetup.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios (ADMIN). /me es para cualquier usuario autenticado.")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @Operation(summary = "Ver mi perfil (usuario autenticado)")
    public UsuarioResponse miPerfil(Authentication auth) {
        return service.perfil(auth.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Editar mi perfil (usuario autenticado)")
    public UsuarioResponse editarMiPerfil(Authentication auth, @Valid @RequestBody PerfilRequest r) {
        return service.actualizarPerfil(auth.getName(), r);
    }

    @GetMapping
    @Operation(summary = "Listar usuarios (ADMIN)")
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id (ADMIN)")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario con cualquier rol (ADMIN)")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest r) {
        return service.crear(r);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario (ADMIN)")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario (ADMIN)")
    public void eliminar(@PathVariable Long id, Authentication auth) {
        service.eliminar(id, auth.getName());
    }
}
