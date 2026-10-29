package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ServicioRequestDTO(
        @NotBlank
                @Pattern(regexp = "^SRV-\\d{3}$", message = "El código debe tener el formato SRV-000")
                String codigo,
        @NotBlank @Size(min = 3, max = 80) String nombre,
        @Size(max = 250) String descripcion,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 8, fraction = 2)
                BigDecimal precio,
        @NotNull @Min(15) @Max(480) Integer duracionMinutos) {}
