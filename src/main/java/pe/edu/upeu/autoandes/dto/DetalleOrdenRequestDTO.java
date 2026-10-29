package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetalleOrdenRequestDTO(
        @NotNull Long servicioId, @NotNull @Min(1) @Max(5) Integer cantidad) {}
