package pe.edu.upeu.autoandes.dto;

import java.math.BigDecimal;

public record IngresoPorServicioResponseDTO(
        String codigoServicio, String servicio, Long cantidadTotal, BigDecimal importeTotal) {}
