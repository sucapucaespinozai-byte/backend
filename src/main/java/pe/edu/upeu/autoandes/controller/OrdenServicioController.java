package pe.edu.upeu.autoandes.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.service.OrdenServicioService;

@RestController
@RequestMapping("/api/v1/ordenes")
@SecurityRequirement(name = "bearerAuth")
public class OrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    public OrdenServicioController(OrdenServicioService ordenServicioService) {
        this.ordenServicioService = ordenServicioService;
    }

    @PostMapping
    public OrdenResponseDTO registrar(@Valid @RequestBody OrdenRequestDTO request) {
        return ordenServicioService.registrar(request);
    }

    @GetMapping
    public List<OrdenResponseDTO> buscar(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) EstadoOrden estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ordenServicioService.buscar(placa, estado, desde, hasta);
    }

    @PatchMapping("/{id}/anular")
    public OrdenResponseDTO anular(
            @PathVariable Long id, @Valid @RequestBody AnularOrdenRequestDTO request) {
        return ordenServicioService.anular(id, request.motivo());
    }
}
