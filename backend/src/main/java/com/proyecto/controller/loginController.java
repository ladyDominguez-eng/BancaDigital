package com.proyecto.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.UsuarioRequest;
import com.proyecto.service.UsuarioService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // CAMBIO: permite cualquier origen (luego puedes poner la URL exacta del frontend)
public class loginController {

    @Autowired
    private UsuarioService usuarioService;
    //define la ruta del enpoint y solo acepta peticiones POST, recibe un objeto JSON con username, password y rol, valida las credenciales y devuelve un objeto JSON indicando si el login fue exitoso o no.
    @PostMapping("/login")
    //responseEntity<?> es una clase de Spring que representa toda la respuesta HTTP, 
    // incluyendo el estado, los encabezados y el cuerpo.
    //  El uso de <?> indica que el tipo de cuerpo de la respuesta puede ser cualquier cosa.
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