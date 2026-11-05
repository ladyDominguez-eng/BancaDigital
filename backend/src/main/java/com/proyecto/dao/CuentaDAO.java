package com.proyecto.dao;

import com.proyecto.model.*;
import com.proyecto.dao.factory.CuentaFactory;
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

    /**
     * Depósito: actualizar el saldo y registrar la transacción deben
     * ocurrir juntos o ninguno (RNF-03). La transacción (commit/rollback)
     * la gestiona ConexionBD.ejecutarTransaccion, en vez de repetir aquí
     * el manejo de autocommit.
     */
    @Override
    public int depositar(String numero, BigDecimal monto) throws SQLException {
        return ConexionBD.getInstancia().ejecutarTransaccion(con -> {
            int cuentaId;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE cuenta SET saldo = saldo + ? WHERE numero = ? AND estado = ? RETURNING id")) {
                ps.setBigDecimal(1, monto);
                ps.setString(2, numero);
                ps.setString(3, EstadoCuenta.ACTIVA);
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

            return transaccionId;
        });
    }
}