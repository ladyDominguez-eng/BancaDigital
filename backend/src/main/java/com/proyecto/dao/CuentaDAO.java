package com.proyecto.dao;

import com.proyecto.model.*;
import com.proyecto.util.ConexionBD;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO implements ICuentaDAO {

    private static final String SELECT =
        "SELECT c.id, c.numero, c.tipo, c.saldo, c.estado, cl.dni, "
      + "cl.nombres || ' ' || cl.apellidos AS titular "
      + "FROM cuenta c JOIN cliente cl ON cl.id = c.cliente_id ";

    // Crea la subclase correcta según el tipo (polimorfismo)
    private Cuenta mapear(ResultSet rs) throws SQLException {
        Cuenta c = "CORRIENTE".equals(rs.getString("tipo"))
                   ? new CuentaCorriente() : new CuentaAhorro();
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

    @Override
    public synchronized int depositar(String numero, BigDecimal monto) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try {
            con.setAutoCommit(false);

            int cuentaId;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE cuenta SET saldo = saldo + ? WHERE numero = ? AND estado = 'ACTIVA' RETURNING id")) {
                ps.setBigDecimal(1, monto);
                ps.setString(2, numero);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Cuenta no encontrada o no activa");
                    cuentaId = rs.getInt(1);
                }
            }

            int transaccionId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transaccion (cuenta_destino_id, tipo, monto) VALUES (?, 'DEPOSITO', ?)",
                    new String[]{"id"})) {
                ps.setInt(1, cuentaId);
                ps.setBigDecimal(2, monto);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    transaccionId = rs.getInt(1);
                }
            }

            con.commit();
            return transaccionId;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }
}