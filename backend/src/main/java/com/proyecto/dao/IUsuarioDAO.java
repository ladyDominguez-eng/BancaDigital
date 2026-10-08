package com.proyecto.dao;

import com.proyecto.model.Usuario;
import java.sql.SQLException;
import java.util.List;

public interface IUsuarioDAO {
    Usuario buscarPorUsername(String username) throws SQLException;
    List<Usuario> listar() throws SQLException;
    int crear(String username, String passwordHash, String rol) throws SQLException;
    boolean actualizarRol(int id, String rol) throws SQLException;
    boolean cambiarEstado(int id, boolean activo) throws SQLException;
}