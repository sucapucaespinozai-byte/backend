package pe.edu.upeu.autoandes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByActivoOrderByApellidosAscNombresAsc(String activo);
}
