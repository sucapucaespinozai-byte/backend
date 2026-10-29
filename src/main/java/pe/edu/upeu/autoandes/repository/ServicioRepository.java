package pe.edu.upeu.autoandes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.entity.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    List<Servicio> findAllByOrderByNombreAsc();

    @Query("""
            select count(d)
            from DetalleOrden d
            where d.servicio.id = :servicioId
            """)
    long countUsosByServicioId(@Param("servicioId") Long servicioId);
}
