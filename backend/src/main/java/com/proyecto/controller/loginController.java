package com.proyecto.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.Usuario;
import com.proyecto.model.UsuarioRequest;
import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.GestorSesiones;
import com.proyecto.seguridad.Sesion;
import com.proyecto.service.UsuarioService;

@RestController
@RequestMapping("/api")
// CORS: antes permitía "*" (cualquier origen); ahora lo aplica CorsConfig solo a los orígenes del proyecto
public class loginController {

    private static final Logger log = LoggerFactory.getLogger(loginController.class);

    @Autowired
    private UsuarioService usuarioService;

    // Define la ruta del endpoint (solo POST). Recibe username, password y rol,
    // valida las credenciales y, si son correctas, crea una sesión y devuelve
    // un token que el frontend enviará en cada petición (RNF-01).
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioRequest request) {

        // Registro con logger (no System.out) y sin datos sensibles
        log.info("Intento de inicio de sesión del usuario {}", request.getUsername());

        Usuario usuario = usuarioService.autenticar(
            request.getUsername(),
            request.getPassword(),
            request.getRol()
        );

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Credenciales inválidas"));
        }

        String token = GestorSesiones.getInstancia().crear(
            new Sesion(usuario.getId(), usuario.getUsername(), usuario.getRol()));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "rol", usuario.getRol(),
            "username", usuario.getUsername(),
            "token", token));
    }

    // Cierra la sesión: el token deja de ser válido en el servidor
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req) {
        GestorSesiones.getInstancia().cerrar(AuthInterceptor.extraerToken(req));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
