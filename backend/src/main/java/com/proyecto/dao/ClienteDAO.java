package com.proyecto.dao;

import com.proyecto.model.Cliente;
import com.proyecto.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteDAO implements IClienteDAO {

    @Override
    public Cliente buscarPorUsuarioId(int usuarioId) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id, usuario_id, dni, nombres, apellidos FROM cliente WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Cliente(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("dni"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"));
            }
        }
    }
}
