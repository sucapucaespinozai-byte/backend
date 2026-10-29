package pe.edu.upeu.autoandes.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.DetalleOrdenResponseDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;

@Component
public class OrdenServicioMapper {

    public OrdenResponseDTO toResponse(OrdenServicio orden) {
        List<DetalleOrdenResponseDTO> detalles =
                orden.getDetalles().stream().map(this::toDetalleResponse).toList();
        return new OrdenResponseDTO(
                orden.getId(),
                orden.getCodigo(),
                orden.getVehiculo().getId(),
                orden.getVehiculo().getPlaca(),
                orden.getKilometraje(),
                orden.getFechaCreacion(),
                orden.getEstado().name(),
                orden.getTotal(),
                orden.getMotivoAnulacion(),
                detalles);
    }

    private DetalleOrdenResponseDTO toDetalleResponse(DetalleOrden detalle) {
        return new DetalleOrdenResponseDTO(
                detalle.getId(),
                detalle.getServicio().getId(),
                detalle.getServicio().getCodigo(),
                detalle.getServicio().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}
