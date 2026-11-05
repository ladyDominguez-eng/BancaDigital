package com.proyecto.controller;

import com.proyecto.model.ClienteRegistrado;
import com.proyecto.model.RegistrarClienteRequest;
import com.proyecto.model.Usuario;
import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.Sesion;
import com.proyecto.service.RegistroClienteService;
import com.proyecto.service.UsuarioService;
import com.proyecto.model.CrearUsuarioRequest;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.RequestAttribute;
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
// CORS: ya no se declara aquí, lo aplica CorsConfig a toda la API
public class UsuarioController {

    // GRASP Controlador: solo coordina; la lógica y el acceso a datos
    // van en el Service. Antes este controlador también recibía el DAO y
    // lo usaba directamente, saltándose la capa de negocio.
    private final UsuarioService usuarioService;
    private final RegistroClienteService registroClienteService;

    public UsuarioController(UsuarioService usuarioService,
            RegistroClienteService registroClienteService) {
        this.usuarioService = usuarioService;
        this.registroClienteService = registroClienteService;
    }

    // ------------------- MÉTODOS DEL CONTROLADOR -------------------

    @GetMapping // Maneja peticiones GET a /api/usuarios
    public List<Usuario> listar() {
        // Devuelve una lista con todos los usuarios registrados en la base de datos
        return usuarioService.listarUsuarios();
    }

    @PostMapping // Maneja peticiones POST a /api/usuarios
    public Usuario crear(
        @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
        @RequestBody CrearUsuarioRequest datos
    ) {
        // Recibe los datos del nuevo usuario desde el frontend (JSON)
        // Llama al servicio para crear el usuario con username, password y rol;
        // la sesión dice qué administrador lo hizo (para la auditoría)
        return usuarioService.crearUsuario(
            sesion.usuarioId(),
            datos.getUsername(),
            datos.getPassword(),
            datos.getRol()
        );
    }

    // Alta de un cliente completo: usuario + datos personales + primera cuenta
    @PostMapping("/clientes")
    public ClienteRegistrado registrarCliente(
        @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
        @RequestBody RegistrarClienteRequest datos
    ) throws SQLException {
        return registroClienteService.registrar(sesion.usuarioId(), datos);
    }

    // Cambia el rol de un usuario del personal. Ejemplo del JSON: { "rol": "CAJERO" }
    @PutMapping("/{id}/rol")
    public Usuario cambiarRol(
        @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
        @PathVariable Long id,
        @RequestBody Map<String, String> datos
    ) {
        return usuarioService.cambiarRol(sesion.usuarioId(), id, datos.get("rol"));
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
