package cl.prosetup.backend.controller;

import cl.prosetup.backend.dto.DashboardResponse;
import cl.prosetup.backend.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración", description = "Estadísticas del panel (ADMIN/VENDEDOR).")
public class AdminController {

    private final OrdenService ordenes;

    public AdminController(OrdenService ordenes) {
        this.ordenes = ordenes;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Estadísticas del dashboard (ADMIN/VENDEDOR)")
    public DashboardResponse dashboard() {
        return ordenes.dashboard();
    }
}
