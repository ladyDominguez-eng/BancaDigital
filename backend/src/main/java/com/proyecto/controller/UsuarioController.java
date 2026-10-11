package com.proyecto.controller;

import com.proyecto.dao.UsuarioDAO;
import com.proyecto.model.Usuario;
import com.proyecto.service.UsuarioService;
import com.proyecto.model.CrearUsuarioRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController // Indica que esta clase es un controlador REST (devuelve JSON)
@RequestMapping("/api/usuarios") // Define la ruta base para todas las peticiones de usuarios

@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://bancadigital-frontend.onrender.com"
})
// Permite que el frontend Angular acceda al backend sin errores CORS
public class UsuarioController {

    // Inyección de dependencias: DAO para acceso a datos y Service para lógica de negocio
    private final UsuarioDAO usuarioDAO;
    private final UsuarioService usuarioService;

    // Constructor que recibe las dependencias necesarias
    public UsuarioController(UsuarioDAO usuarioDAO, UsuarioService usuarioService) {
        this.usuarioDAO = usuarioDAO;
        this.usuarioService = usuarioService;
    }

    // ------------------- MÉTODOS DEL CONTROLADOR -------------------

    @GetMapping // Maneja peticiones GET a /api/usuarios
    public List<Usuario> listar() {
        // Devuelve una lista con todos los usuarios registrados en la base de datos
        return usuarioDAO.listarTodos();
    }

    @PostMapping // Maneja peticiones POST a /api/usuarios
    public Usuario crear(@RequestBody CrearUsuarioRequest datos) {
        // Recibe los datos del nuevo usuario desde el frontend (JSON)
        // Llama al servicio para crear el usuario con username, password y rol
        return usuarioService.crearUsuario(
            datos.getUsername(),
            datos.getPassword(),
            datos.getRol()
        );
    }

    @PutMapping("/{id}/estado") // Maneja peticiones PUT a /api/usuarios/{id}/estado
    public boolean cambiarEstado(
        @PathVariable Long id, // Obtiene el ID del usuario desde la URL
        @RequestBody Map<String, Boolean> datos // Recibe el nuevo estado (activo/inactivo) en formato JSON
    ) {
        // Llama al servicio para cambiar el estado del usuario según el ID
        // Ejemplo del JSON recibido: { "activo": true }
        return usuarioService.cambiarEstado(
            id,
            datos.get("activo")
        );
    }
}
