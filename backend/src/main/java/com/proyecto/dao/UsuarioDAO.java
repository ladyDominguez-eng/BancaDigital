package com.proyecto.dao;

import com.proyecto.model.Usuario;
import com.proyecto.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
@Repository 
public class UsuarioDAO {

    private Connection conexion;

    public UsuarioDAO() {
        this.conexion = ConexionBD.getInstancia().getConexion();
    }

    public Usuario buscarPorUsername(String username) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                "SELECT * FROM usuario WHERE username = ?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRol(rs.getString("rol"));
                u.setActivo(rs.getBoolean("activo"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        try {
            PreparedStatement ps = conexion.prepareStatement("SELECT * FROM usuario");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRol(rs.getString("rol"));
                u.setActivo(rs.getBoolean("activo"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Usuario crear(Usuario usuario) {
    String sql = """
        INSERT INTO usuario (username, password_hash, rol, activo)
        VALUES (?, ?, ?, true)
        RETURNING id, username, password_hash, rol, activo
        """;

    try {
        PreparedStatement ps = conexion.prepareStatement(sql);

        ps.setString(1, usuario.getUsername());
        ps.setString(2, usuario.getPasswordHash());
        ps.setString(3, usuario.getRol());

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Usuario nuevo = new Usuario();

            nuevo.setId(rs.getLong("id"));
            nuevo.setUsername(rs.getString("username"));
            nuevo.setPasswordHash(rs.getString("password_hash"));
            nuevo.setRol(rs.getString("rol"));
            nuevo.setActivo(rs.getBoolean("activo"));

            return nuevo;
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}
public boolean cambiarEstado(Long id, boolean activo) {

    String sql = "UPDATE usuario SET activo = ? WHERE id = ?";

    try {

        PreparedStatement ps = conexion.prepareStatement(sql);

        ps.setBoolean(1, activo);
        ps.setLong(2, id);

        int filas = ps.executeUpdate();

        return filas > 0;

    } catch (SQLException e) {

        e.printStackTrace();
        return false;
    }
}

}
