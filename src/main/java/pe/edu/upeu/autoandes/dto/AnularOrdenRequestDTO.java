package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularOrdenRequestDTO(
        @NotBlank @Size(min = 10, max = 200) String motivo) {}
