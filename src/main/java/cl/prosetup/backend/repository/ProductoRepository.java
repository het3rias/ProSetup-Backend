package cl.prosetup.backend.repository;

import cl.prosetup.backend.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCategoriaId(Long categoriaId);

    /** Bloquea la fila mientras se descuenta el stock, para que dos compras simultáneas no vendan de más. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") Long id);
}
