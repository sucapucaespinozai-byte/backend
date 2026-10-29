package pe.edu.upeu.autoandes.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        OffsetDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> campos) {}
