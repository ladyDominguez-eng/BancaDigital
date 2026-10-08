package com.proyecto.controller;

import com.proyecto.model.InformesCuentas;
import com.proyecto.service.InformesService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/informes")
@CrossOrigin(origins = "http://localhost:4200")
public class InformesController {

    private final InformesService service = new InformesService();

    @GetMapping("/usuarios")
    public ResponseEntity<?> usuariosPorRol() {

        try {
            return ResponseEntity.ok(
                service.obtenerUsuariosPorRol()
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener informe de usuarios.");
        }
    }


    @GetMapping("/cuentas")
    public ResponseEntity<?> resumenCuentas() {

        try {
            InformesCuentas informe =
                service.obtenerResumenCuentas();

            return ResponseEntity.ok(informe);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener informe de cuentas.");
        }
    }


    @GetMapping("/transacciones")
    public ResponseEntity<?> transaccionesPorTipo() {

        try {
            return ResponseEntity.ok(
                service.obtenerTransaccionesPorTipo()
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener informe de transacciones.");
        }
    }
}
