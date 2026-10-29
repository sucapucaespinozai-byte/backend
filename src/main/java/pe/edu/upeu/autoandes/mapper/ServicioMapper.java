package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.Servicio;

@Component
public class ServicioMapper {

    public Servicio toEntity(ServicioRequestDTO request) {
        Servicio servicio = new Servicio();
        updateEntity(request, servicio);
        servicio.setActivo("S");
        return servicio;
    }

    public void updateEntity(ServicioRequestDTO request, Servicio servicio) {
        servicio.setCodigo(request.codigo());
        servicio.setNombre(request.nombre());
        servicio.setDescripcion(request.descripcion());
        servicio.setPrecio(request.precio());
        servicio.setDuracionMinutos(request.duracionMinutos());
    }

    public ServicioResponseDTO toResponse(Servicio servicio) {
        return new ServicioResponseDTO(
                servicio.getId(),
                servicio.getCodigo(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getPrecio(),
                servicio.getDuracionMinutos(),
                "S".equals(servicio.getActivo()));
    }
}
