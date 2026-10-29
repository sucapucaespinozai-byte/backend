package pe.edu.upeu.autoandes.dto;

public record ClienteResponseDTO(
        Long id, String nombres, String apellidos, String documento, String telefono, String correo) {}
