package com.proyecto.controller;

import com.proyecto.service.InformesService;

import java.sql.SQLException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador: delega en el Service. Errores y CORS centralizados
 * en ManejadorErrores y CorsConfig.
 */
@RestController
@RequestMapping("/api/informes")
public class InformesController {

    private final InformesService service = new InformesService();

    @GetMapping("/usuarios")
    public ResponseEntity<?> usuariosPorRol() throws SQLException {
        return ResponseEntity.ok(service.obtenerUsuariosPorRol());
    }

    @GetMapping("/cuentas")
    public ResponseEntity<?> resumenCuentas() throws SQLException {
        return ResponseEntity.ok(service.obtenerResumenCuentas());
    }

    @GetMapping("/transacciones")
    public ResponseEntity<?> transaccionesPorTipo() throws SQLException {
        return ResponseEntity.ok(service.obtenerTransaccionesPorTipo());
    }
}
