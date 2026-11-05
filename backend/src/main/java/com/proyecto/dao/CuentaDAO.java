package com.proyecto.dao;

import com.proyecto.model.*;
import com.proyecto.dao.factory.CuentaFactory;
import com.proyecto.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO implements ICuentaDAO {

    private static final String SELECT =
        "SELECT c.id, c.numero, c.tipo, c.saldo, c.estado, cl.dni, "
      + "cl.nombres || ' ' || cl.apellidos AS titular "
      + "FROM cuenta c JOIN cliente cl ON cl.id = c.cliente_id ";

    // La subclase correcta (polimorfismo) la crea CuentaFactory según el tipo,
    // así este DAO no cambia cuando se agregue un tipo de cuenta nuevo.
    private Cuenta mapear(ResultSet rs) throws SQLException {
        Cuenta c = CuentaFactory.crear(rs.getString("tipo"));
        c.setId(rs.getInt("id"));
        c.setNumero(rs.getString("numero"));
        c.setSaldo(rs.getBigDecimal("saldo"));
        c.setEstado(rs.getString("estado"));
        c.setDni(rs.getString("dni"));
        c.setTitular(rs.getString("titular"));
        return c;
    }

    @Override
    public Cuenta buscarPorNumero(String numero) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE c.numero = ?")) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Cuenta buscarPorId(int id) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Cuenta buscarDeUsuario(int cuentaId, int usuarioId) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                SELECT + "WHERE c.id = ? AND cl.usuario_id = ?")) {
            ps.setInt(1, cuentaId);
            ps.setInt(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Cuenta> listarPorUsuario(int usuarioId) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        List<Cuenta> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                SELECT + "WHERE cl.usuario_id = ? ORDER BY c.id")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Cuenta> buscarPorDni(String dni) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        List<Cuenta> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE cl.dni = ? ORDER BY c.id")) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }
}
