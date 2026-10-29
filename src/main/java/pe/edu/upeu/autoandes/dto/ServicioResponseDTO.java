package pe.edu.upeu.autoandes.dto;

import java.math.BigDecimal;

public record ServicioResponseDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer duracionMinutos,
        boolean activo) {}
