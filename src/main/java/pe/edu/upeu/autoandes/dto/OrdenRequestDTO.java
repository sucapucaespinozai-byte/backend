package pe.edu.upeu.autoandes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrdenRequestDTO(
        @NotNull Long vehiculoId,
        @NotNull @PositiveOrZero Integer kilometraje,
        @NotEmpty @Size(max = 10) List<@Valid DetalleOrdenRequestDTO> detalles) {}
