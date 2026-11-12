package cl.prosetup.backend.repository;

import cl.prosetup.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCategoriaId(Long categoriaId);
}
