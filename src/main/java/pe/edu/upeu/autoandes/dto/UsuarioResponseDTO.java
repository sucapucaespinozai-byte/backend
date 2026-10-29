package pe.edu.upeu.autoandes.dto;

import java.util.List;

public record UsuarioResponseDTO(
        Long id,
        String username,
        String nombreCompleto,
        boolean activo,
        List<String> roles,
        List<String> permisos) {}
