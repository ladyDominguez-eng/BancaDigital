package com.proyecto.controller;

import com.proyecto.dao.IBitacoraDAO;
import com.proyecto.service.AuditoriaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * GRASP Controlador: convierte los parámetros y delega en el Service.
 * Errores y CORS centralizados en ManejadorErrores y CorsConfig. Una fecha
 * con formato inválido ahora responde 400 (antes respondía 500).
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(IBitacoraDAO dao) {
        this.service = new AuditoriaService(dao);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta
    ) throws SQLException {

        LocalDate desde = fechaDesde != null && !fechaDesde.isBlank()
                ? LocalDate.parse(fechaDesde)
                : null;

        LocalDate hasta = fechaHasta != null && !fechaHasta.isBlank()
                ? LocalDate.parse(fechaHasta)
                : null;

        return ResponseEntity.ok(service.listar(usuarioId, desde, hasta));
    }
}
