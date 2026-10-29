package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequestDTO(@NotNull Boolean activo) {}
