package com.proyecto.dao;

import com.proyecto.model.SolicitudCredito;
import com.proyecto.util.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SolicitudCreditoDAO implements ISolicitudCreditoDAO {

    private static final String SELECT = """
        SELECT s.id, s.cliente_id, c.nombres || ' ' || c.apellidos AS cliente, c.dni,
               s.asesor_id, s.monto, s.plazo_meses, s.estado, s.observaciones, s.fecha
        FROM solicitud_credito s
        JOIN cliente c ON c.id = s.cliente_id
        """;

    // Un único lugar que convierte una fila en SolicitudCredito
    private SolicitudCredito mapear(ResultSet rs) throws SQLException {
        SolicitudCredito s = new SolicitudCredito();
        s.setId(rs.getInt("id"));
        s.setClienteId(rs.getInt("cliente_id"));
        s.setClienteNombre(rs.getString("cliente"));
        s.setClienteDni(rs.getString("dni"));
        int asesor = rs.getInt("asesor_id");
        s.setAsesorId(rs.wasNull() ? null : asesor);
        s.setMonto(rs.getBigDecimal("monto"));
        s.setPlazoMeses(rs.getInt("plazo_meses"));
        s.setEstado(rs.getString("estado")); // State: se reconstruye el objeto de estado
        s.setObservaciones(rs.getString("observaciones"));
        Timestamp f = rs.getTimestamp("fecha");
        s.setFecha(f == null ? null : f.toLocalDateTime());
        return s;
    }

    @Override
    public int crear(int clienteId, BigDecimal monto, int plazoMeses) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO solicitud_credito (cliente_id, monto, plazo_meses) VALUES (?, ?, ?)",
                new String[]{"id"})) {
            ps.setInt(1, clienteId);
            ps.setBigDecimal(2, monto);
            ps.setInt(3, plazoMeses);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public SolicitudCredito buscarPorId(int id) throws SQLException {
        List<SolicitudCredito> lista = consultar(SELECT + "WHERE s.id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<SolicitudCredito> listarPorCliente(int clienteId) throws SQLException {
        return consultar(SELECT + "WHERE s.cliente_id = ? ORDER BY s.fecha DESC, s.id DESC", clienteId);
    }

    @Override
    public List<SolicitudCredito> listar(String estado) throws SQLException {
        if (estado == null || estado.isBlank()) {
            return consultar(SELECT + "ORDER BY s.fecha DESC, s.id DESC");
        }
        return consultar(SELECT + "WHERE s.estado = ? ORDER BY s.fecha DESC, s.id DESC", estado);
    }

    @Override
    public boolean actualizarEstado(int id, String estadoAnterior, String estadoNuevo,
            Integer asesorId, String observaciones) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        // "AND estado = ?" garantiza que solo se cambie si nadie más la modificó
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE solicitud_credito SET estado = ?, asesor_id = ?, "
              + "observaciones = COALESCE(?, observaciones) WHERE id = ? AND estado = ?")) {
            ps.setString(1, estadoNuevo);
            if (asesorId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, asesorId);
            }
            ps.setString(3, observaciones);
            ps.setInt(4, id);
            ps.setString(5, estadoAnterior);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizarObservaciones(int id, int asesorId, String observaciones) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE solicitud_credito SET observaciones = ?, asesor_id = ? WHERE id = ?")) {
            ps.setString(1, observaciones);
            ps.setInt(2, asesorId);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<SolicitudCredito> consultar(String sql, Object... parametros) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        List<SolicitudCredito> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }
}
