package pe.edu.upeu.autoandes.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.IngresoPorServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.exception.InvalidRequestException;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;

@Service
public class ReporteService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Lima");

    private final OrdenServicioRepository ordenServicioRepository;

    public ReporteService(OrdenServicioRepository ordenServicioRepository) {
        this.ordenServicioRepository = ordenServicioRepository;
    }

    @Transactional(readOnly = true)
    public List<IngresoPorServicioResponseDTO> ingresosPorServicio(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new InvalidRequestException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        OffsetDateTime inicio = desde == null ? null : desde.atStartOfDay(ZONA_HORARIA).toOffsetDateTime();
        OffsetDateTime finExclusivo =
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA_HORARIA).toOffsetDateTime();
        return ordenServicioRepository
                .ingresosPorServicio(EstadoOrden.ANULADA, inicio, finExclusivo)
                .stream()
                .map(item -> new IngresoPorServicioResponseDTO(
                        item.getCodigoServicio(),
                        item.getServicio(),
                        item.getCantidadTotal(),
                        item.getImporteTotal()))
                .toList();
    }
}
