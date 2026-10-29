package pe.edu.upeu.autoandes.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.ConflictException;
import pe.edu.upeu.autoandes.exception.ResourceNotFoundException;
import pe.edu.upeu.autoandes.mapper.VehiculoMapper;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;

@Service
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoService(
            VehiculoRepository vehiculoRepository,
            ClienteRepository clienteRepository,
            OrdenServicioRepository ordenServicioRepository,
            VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.ordenServicioRepository = ordenServicioRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listar(Long clienteId) {
        if (clienteId == null) {
            return vehiculoRepository.findAllByOrderByPlacaAsc().stream()
                    .map(vehiculoMapper::toResponse)
                    .toList();
        }
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("No existe el cliente solicitado");
        }
        return vehiculoRepository.findAllByCliente_IdOrderByPlacaAsc(clienteId).stream()
                .map(vehiculoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtener(Long id) {
        return vehiculoMapper.toResponse(buscarPorId(id));
    }

    public VehiculoResponseDTO crear(VehiculoRequestDTO request) {
        validarPlacaDuplicada(request.placa(), null);
        Cliente cliente = buscarCliente(request.clienteId());
        Vehiculo vehiculo = vehiculoMapper.toEntity(request, cliente);
        return vehiculoMapper.toResponse(vehiculoRepository.save(vehiculo));
    }

    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO request) {
        Vehiculo vehiculo = buscarPorId(id);
        validarPlacaDuplicada(request.placa(), id);
        Cliente cliente = buscarCliente(request.clienteId());
        vehiculoMapper.updateEntity(request, cliente, vehiculo);
        return vehiculoMapper.toResponse(vehiculoRepository.save(vehiculo));
    }

    public void eliminar(Long id) {
        Vehiculo vehiculo = buscarPorId(id);
        if (ordenServicioRepository.countByVehiculo_Id(id) > 0) {
            throw new ConflictException("No se puede eliminar un vehículo que tiene órdenes de servicio");
        }
        vehiculoRepository.delete(vehiculo);
    }

    private Vehiculo buscarPorId(Long id) {
        return vehiculoRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el vehículo solicitado"));
    }

    private Cliente buscarCliente(Long clienteId) {
        return clienteRepository
                .findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el cliente indicado"));
    }

    private void validarPlacaDuplicada(String placa, Long idActual) {
        boolean duplicada = idActual == null
                ? vehiculoRepository.existsByPlaca(placa)
                : vehiculoRepository.existsByPlacaAndIdNot(placa, idActual);
        if (duplicada) {
            throw new ConflictException("Ya existe un vehículo con esa placa");
        }
    }
}
