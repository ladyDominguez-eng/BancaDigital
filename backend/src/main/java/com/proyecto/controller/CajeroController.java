package com.proyecto.controller;

import com.proyecto.model.DepositoRequest;
import com.proyecto.service.CajeroService;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador: recibe la petición y delega en el Service.
 * Los errores y el CORS ya no se repiten aquí: los atienden
 * ManejadorErrores y CorsConfig.
 */
@RestController
@RequestMapping("/api/cajero")
public class CajeroController {

    private final CajeroService service = new CajeroService();

    @GetMapping("/cuentas")
    public ResponseEntity<?> consultar(@RequestParam(required = false) String dni,
                                       @RequestParam(required = false) String numero)
            throws SQLException {
        return ResponseEntity.ok(service.consultar(dni, numero));
    }

    @PostMapping("/depositos")
    public ResponseEntity<?> depositar(@RequestBody DepositoRequest req)
            throws SQLException {
        int id = service.depositar(req.getNumeroCuenta(), req.getMonto());
        return ResponseEntity.ok(Map.of("mensaje", "Depósito registrado.", "transaccionId", id));
    }
}
