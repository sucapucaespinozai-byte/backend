package pe.edu.upeu.autoandes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    List<Vehiculo> findAllByOrderByPlacaAsc();

    List<Vehiculo> findAllByCliente_IdOrderByPlacaAsc(Long clienteId);
}
