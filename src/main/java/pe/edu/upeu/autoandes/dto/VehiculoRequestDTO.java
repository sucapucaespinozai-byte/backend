package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VehiculoRequestDTO(
        @NotBlank
                @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "La placa debe tener el formato ABC-123")
                String placa,
        @NotBlank @Size(max = 60) String marca,
        @NotBlank @Size(max = 60) String modelo,
        @NotNull @Min(1990) @Max(2027) Integer anio,
        @NotNull Long clienteId) {}
