package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.Vehiculo;

@Component
public class VehiculoMapper {

    public Vehiculo toEntity(VehiculoRequestDTO request, Cliente cliente) {
        Vehiculo vehiculo = new Vehiculo();
        updateEntity(request, cliente, vehiculo);
        vehiculo.setKilometrajeActual(0);
        return vehiculo;
    }

    public void updateEntity(VehiculoRequestDTO request, Cliente cliente, Vehiculo vehiculo) {
        vehiculo.setPlaca(request.placa());
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnio(request.anio());
        vehiculo.setCliente(cliente);
    }

    public VehiculoResponseDTO toResponse(Vehiculo vehiculo) {
        Cliente cliente = vehiculo.getCliente();
        return new VehiculoResponseDTO(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getKilometrajeActual(),
                cliente.getId(),
                cliente.getNombres() + " " + cliente.getApellidos());
    }
}
