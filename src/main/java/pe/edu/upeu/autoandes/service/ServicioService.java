package pe.edu.upeu.autoandes.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.ConflictException;
import pe.edu.upeu.autoandes.exception.ResourceNotFoundException;
import pe.edu.upeu.autoandes.mapper.ServicioMapper;
import pe.edu.upeu.autoandes.repository.ServicioRepository;

@Service
@Transactional
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;

    public ServicioService(ServicioRepository servicioRepository, ServicioMapper servicioMapper) {
        this.servicioRepository = servicioRepository;
        this.servicioMapper = servicioMapper;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listar() {
        return servicioRepository.findAllByOrderByNombreAsc().stream()
                .map(servicioMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponseDTO obtener(Long id) {
        return servicioMapper.toResponse(buscarPorId(id));
    }

    public ServicioResponseDTO crear(ServicioRequestDTO request) {
        if (servicioRepository.existsByCodigo(request.codigo())) {
            throw new ConflictException("Ya existe un servicio con ese código");
        }
        return servicioMapper.toResponse(servicioRepository.save(servicioMapper.toEntity(request)));
    }

    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO request) {
        Servicio servicio = buscarPorId(id);
        if (servicioRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new ConflictException("Ya existe un servicio con ese código");
        }
        servicioMapper.updateEntity(request, servicio);
        return servicioMapper.toResponse(servicioRepository.save(servicio));
    }

    public void eliminar(Long id) {
        Servicio servicio = buscarPorId(id);
        if (servicioRepository.countUsosByServicioId(id) > 0) {
            throw new ConflictException(
                    "El servicio ya fue utilizado en una orden; cambie su estado en lugar de eliminarlo");
        }
        servicioRepository.delete(servicio);
    }

    public ServicioResponseDTO cambiarEstado(Long id, boolean activo) {
        Servicio servicio = buscarPorId(id);
        servicio.setActivo(activo ? "S" : "N");
        return servicioMapper.toResponse(servicioRepository.save(servicio));
    }

    private Servicio buscarPorId(Long id) {
        return servicioRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el servicio solicitado"));
    }
}
