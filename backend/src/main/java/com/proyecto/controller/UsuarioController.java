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

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioDAO usuarioDAO;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioDAO usuarioDAO, UsuarioService usuarioService) {
        this.usuarioDAO = usuarioDAO;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioDAO.listarTodos();
    }

    @PostMapping
    public Usuario crear(@RequestBody CrearUsuarioRequest datos) {
        return usuarioService.crearUsuario(
            datos.getUsername(),
            datos.getPassword(),
            datos.getRol()
        );
    }
    @PutMapping("/{id}/estado")
public boolean cambiarEstado(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> datos) {

    return usuarioService.cambiarEstado(
        id,
        datos.get("activo")
    );
}
}