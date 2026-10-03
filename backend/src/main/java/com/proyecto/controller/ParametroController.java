package com.proyecto.controller;

import com.proyecto.service.ParametroService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parametros")
@CrossOrigin(origins = "http://localhost:4200")
public class ParametroController {

    private final ParametroService service = new ParametroService();

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(service.listar());
        } catch (Exception e) {
            return error(500, "Error al listar parámetros.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id,
                                        @RequestBody Map<String, String> body) {
        try {
            service.actualizar(id, body.get("valor"));
            return ResponseEntity.ok(Map.of("mensaje", "Parámetro actualizado."));
        } catch (IllegalArgumentException e) {
            return error(400, e.getMessage());
        } catch (Exception e) {
            return error(500, "Error al actualizar el parámetro.");
        }
    }

    private ResponseEntity<?> error(int codigo, String mensaje) {
        return ResponseEntity.status(codigo).body(Map.of("error", mensaje));
    }
}