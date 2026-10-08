package com.proyecto.controller;

import com.proyecto.model.DepositoRequest;
import com.proyecto.service.CajeroService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cajero")
@CrossOrigin(origins = "http://localhost:4200")
public class CajeroController {

    private final CajeroService service = new CajeroService();

    @GetMapping("/cuentas")
    public ResponseEntity<?> consultar(@RequestParam(required = false) String dni,
                                       @RequestParam(required = false) String numero) {
        try {
            return ResponseEntity.ok(service.consultar(dni, numero));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al consultar cuentas."));
        }
    }

    @PostMapping("/depositos")
    public ResponseEntity<?> depositar(@RequestBody DepositoRequest req) {
        try {
            int id = service.depositar(req.getNumeroCuenta(), req.getMonto());
            return ResponseEntity.ok(Map.of("mensaje", "Depósito registrado.", "transaccionId", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar el depósito."));
        }
    }
}