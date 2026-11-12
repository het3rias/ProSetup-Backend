package cl.prosetup.backend.repository;

import cl.prosetup.backend.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findAllByOrderByFechaDesc();
    List<Orden> findByUsuarioCorreoIgnoreCaseOrderByFechaDesc(String correo);
}
