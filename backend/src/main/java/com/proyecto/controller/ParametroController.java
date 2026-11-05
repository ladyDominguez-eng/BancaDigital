package com.proyecto.controller;

import com.proyecto.service.ParametroService;

import java.sql.SQLException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador: delega en el Service. Errores y CORS centralizados
 * en ManejadorErrores y CorsConfig.
 */
@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    private final ParametroService service = new ParametroService();

    @GetMapping
    public ResponseEntity<?> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) throws SQLException {

        service.actualizar(id, body.get("valor"));
        return ResponseEntity.ok(Map.of("mensaje", "Parámetro actualizado."));
    }
}
