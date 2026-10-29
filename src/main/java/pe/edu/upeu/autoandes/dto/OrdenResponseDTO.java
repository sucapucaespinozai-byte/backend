package pe.edu.upeu.autoandes.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrdenResponseDTO(
        Long id,
        String codigo,
        Long vehiculoId,
        String placa,
        Integer kilometraje,
        OffsetDateTime fechaCreacion,
        String estado,
        BigDecimal total,
        String motivoAnulacion,
        List<DetalleOrdenResponseDTO> detalles) {}
