package com.proyecto.controller;

import com.proyecto.dao.IBitacoraDAO;
import com.proyecto.model.Bitacora;
import com.proyecto.service.AuditoriaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200")
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
    ) {

        try {

            LocalDate desde = fechaDesde != null && !fechaDesde.isBlank()
                    ? LocalDate.parse(fechaDesde)
                    : null;

            LocalDate hasta = fechaHasta != null && !fechaHasta.isBlank()
                    ? LocalDate.parse(fechaHasta)
                    : null;

            List<Bitacora> lista =
                    service.listar(usuarioId, desde, hasta);

            return ResponseEntity.ok(lista);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Error al obtener la auditoría.");
        }
    }
}