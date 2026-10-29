package pe.edu.upeu.autoandes.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.DetalleOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.ConflictException;
import pe.edu.upeu.autoandes.exception.InvalidRequestException;
import pe.edu.upeu.autoandes.exception.ResourceNotFoundException;
import pe.edu.upeu.autoandes.mapper.OrdenServicioMapper;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;

@Service
public class OrdenServicioService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Lima");

    private final OrdenServicioRepository ordenServicioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ServicioRepository servicioRepository;
    private final OrdenServicioMapper ordenServicioMapper;

    public OrdenServicioService(
            OrdenServicioRepository ordenServicioRepository,
            VehiculoRepository vehiculoRepository,
            ServicioRepository servicioRepository,
            OrdenServicioMapper ordenServicioMapper) {
        this.ordenServicioRepository = ordenServicioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.servicioRepository = servicioRepository;
        this.ordenServicioMapper = ordenServicioMapper;
    }

    @Transactional
    public OrdenResponseDTO registrar(OrdenRequestDTO request) {
        if (request == null || request.vehiculoId() == null || request.kilometraje() == null
                || request.kilometraje() < 0) {
            throw new InvalidRequestException("La orden debe indicar vehículo y un kilometraje válido");
        }
        if (request.detalles() == null
                || request.detalles().isEmpty()
                || request.detalles().size() > 10) {
            throw new ConflictException("La orden debe contener entre 1 y 10 detalles");
        }

        Set<Long> idsServicio = new HashSet<>();
        for (DetalleOrdenRequestDTO detalle : request.detalles()) {
            if (detalle == null || detalle.servicioId() == null || detalle.cantidad() == null) {
                throw new InvalidRequestException("Cada detalle debe indicar servicio y cantidad");
            }
            if (detalle.cantidad() < 1 || detalle.cantidad() > 5) {
                throw new ConflictException("La cantidad de cada detalle debe estar entre 1 y 5");
            }
            if (!idsServicio.add(detalle.servicioId())) {
                throw new ConflictException("No se puede repetir un servicio en la misma orden");
            }
        }

        Vehiculo vehiculo = vehiculoRepository
                .findById(request.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el vehículo indicado"));
        int ultimoKilometraje = vehiculo.getKilometrajeActual();
        var ultimaOrden = ordenServicioRepository
                .findTopByVehiculo_IdOrderByFechaCreacionDescIdDesc(vehiculo.getId());
        if (ultimaOrden.isPresent()) {
            ultimoKilometraje = Math.max(ultimoKilometraje, ultimaOrden.get().getKilometraje());
        }
        if (request.kilometraje() < ultimoKilometraje) {
            throw new ConflictException(
                    "El kilometraje no puede ser menor al último registrado para el vehículo");
        }

        Map<Long, Servicio> servicios = new HashMap<>();
        servicioRepository.findAllById(idsServicio).forEach(servicio -> servicios.put(servicio.getId(), servicio));
        if (servicios.size() != idsServicio.size()) {
            throw new ResourceNotFoundException("Uno o más servicios indicados no existen");
        }

        OrdenServicio orden = new OrdenServicio();
        orden.setCodigo(generarCodigo());
        orden.setVehiculo(vehiculo);
        orden.setKilometraje(request.kilometraje());
        orden.setFechaCreacion(OffsetDateTime.now(ZONA_HORARIA));
        orden.setEstado(EstadoOrden.REGISTRADA);

        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        for (DetalleOrdenRequestDTO solicitud : request.detalles()) {
            Servicio servicio = servicios.get(solicitud.servicioId());
            if (!"S".equals(servicio.getActivo())) {
                throw new ConflictException("Todos los servicios de la orden deben estar activos");
            }
            BigDecimal subtotal = servicio.getPrecio()
                    .multiply(BigDecimal.valueOf(solicitud.cantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrden(orden);
            detalle.setServicio(servicio);
            detalle.setCantidad(solicitud.cantidad());
            detalle.setPrecioUnitario(servicio.getPrecio());
            detalle.setSubtotal(subtotal);
            orden.getDetalles().add(detalle);
            total = total.add(subtotal);
        }
        orden.setTotal(total);
        vehiculo.setKilometrajeActual(request.kilometraje());

        return ordenServicioMapper.toResponse(ordenServicioRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscar(
            String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new InvalidRequestException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        OffsetDateTime inicio = desde == null ? null : desde.atStartOfDay(ZONA_HORARIA).toOffsetDateTime();
        OffsetDateTime finExclusivo =
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA_HORARIA).toOffsetDateTime();
        return ordenServicioRepository.buscar(placa, estado, inicio, finExclusivo).stream()
                .map(ordenServicioMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrdenResponseDTO anular(Long id, String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.trim().length() < 10 || motivo.trim().length() > 200) {
            throw new InvalidRequestException("El motivo debe tener entre 10 y 200 caracteres");
        }
        OrdenServicio orden = ordenServicioRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la orden solicitada"));
        if (orden.getEstado() != EstadoOrden.REGISTRADA) {
            throw new ConflictException("Solo se pueden anular órdenes registradas");
        }
        if (!orden.getFechaCreacion().toLocalDate().equals(LocalDate.now(ZONA_HORARIA))) {
            throw new ConflictException("Solo se pueden anular órdenes registradas el mismo día");
        }
        orden.setEstado(EstadoOrden.ANULADA);
        orden.setMotivoAnulacion(motivo.trim());
        orden.setAnuladaEn(OffsetDateTime.now(ZONA_HORARIA));
        return ordenServicioMapper.toResponse(ordenServicioRepository.save(orden));
    }

    private String generarCodigo() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
