package com.proyecto.dao;

import com.proyecto.model.Usuario;
import com.proyecto.util.AccesoDatosException;
import com.proyecto.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * DAO de Usuario (JDBC con PreparedStatement).
 *
 * CORRECCIONES DE ESTA VERSIÓN
 * - Copiar y pegar: el mapeo de una fila a Usuario estaba repetido 3 veces;
 *   ahora vive en un solo método (mapear).
 * - Errores tragados: antes se hacía printStackTrace y se devolvía
 *   null/false; ahora se lanza AccesoDatosException.
 * - Conexión: antes se guardaba la conexión en el constructor, así que si
 *   se caía quedaba inútil. Ahora se pide a ConexionBD en cada operación y
 *   así aprovecha la reconexión del Singleton.
 * - Recursos: PreparedStatement y ResultSet se cierran con
 *   try-with-resources.
 */
@Repository
public class UsuarioDAO implements IUsuarioDAO {

    // Un solo lugar que convierte una fila SQL en un objeto Usuario
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM usuario WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar el usuario", e);
        }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM usuario WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar el usuario", e);
        }
    }

    @Override
    public boolean cambiarRol(Long id, String rol) {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE usuario SET rol = ? WHERE id = ?")) {
            ps.setString(1, rol);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al cambiar el rol del usuario", e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        Connection con = ConexionBD.getInstancia().getConexion();
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar los usuarios", e);
        }
        return lista;
    }

    @Override
    public Usuario crear(Usuario usuario) {
        String sql = """
            INSERT INTO usuario (username, password_hash, rol, activo)
            VALUES (?, ?, ?, true)
            RETURNING id, username, password_hash, rol, activo
            """;

        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al crear el usuario", e);
        }
    }

    // Reemplaza el hash guardado (se usa para fortalecer hashes antiguos)
    @Override
    public boolean actualizarPasswordHash(Long id, String nuevoHash) {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE usuario SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, nuevoHash);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar la contraseña", e);
        }
    }

    // Cambiar estado (activar/desactivar)
    @Override
    public boolean cambiarEstado(Long id, boolean activo) {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE usuario SET activo = ? WHERE id = ?")) {
            ps.setBoolean(1, activo);
            ps.setLong(2, id);
            // true si se actualizó al menos una fila
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al cambiar el estado del usuario", e);
        }
    }
}
