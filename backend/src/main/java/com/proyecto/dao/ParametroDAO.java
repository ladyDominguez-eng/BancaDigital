package com.proyecto.dao;

import com.proyecto.model.Parametro;
import com.proyecto.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParametroDAO {

    public List<Parametro> listar() throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        String sql = "SELECT id, nombre, valor, descripcion FROM parametro_sistema ORDER BY id";
        List<Parametro> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Parametro p = new Parametro();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setValor(rs.getString("valor"));
                p.setDescripcion(rs.getString("descripcion"));
                lista.add(p);
            }
        }
        return lista;
    }

    public boolean actualizarValor(int id, String valor) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE parametro_sistema SET valor = ? WHERE id = ?")) {
            ps.setString(1, valor);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}