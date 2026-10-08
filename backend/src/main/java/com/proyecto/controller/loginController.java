package com.proyecto.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.UsuarioRequest;
import com.proyecto.service.UsuarioService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") // Permitir solicitudes desde el frontend
public class loginController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioRequest request) {
        
        System.out.println("🔥 LLEGÓ AL LOGIN: " + request.getUsername());
        
        boolean valido = usuarioService.validarUsuario(
            request.getUsername(),
            request.getPassword(),
            request.getRol()
        );

        if (valido) {
            return ResponseEntity.ok(Map.of("success", true, "rol", request.getRol()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("success", false, "error", "Credenciales inválidas"));
        }
    }
}
