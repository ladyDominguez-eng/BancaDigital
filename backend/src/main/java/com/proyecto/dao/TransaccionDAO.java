package com.proyecto.dao;

import com.proyecto.model.EstadoCuenta;
import com.proyecto.model.TipoTransaccion;
import com.proyecto.model.TransaccionVista;
import com.proyecto.util.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de transacciones y comprobantes (JDBC con PreparedStatement).
 *
 * PROBLEMA: depósito, retiro, transferencia y pago hacen casi lo mismo
 *           (restar a una cuenta, sumar a otra, guardar transacción y
 *           comprobante). Escribir cada uno por separado repetiría el SQL
 *           cuatro veces (antipatrón "copiar y pegar").
 * SOLUCIÓN: un único método privado "registrar" recibe qué cuentas
 *           intervienen; los cuatro públicos solo cambian esos datos.
 *           La transacción (commit/rollback) la gestiona
 *           ConexionBD.ejecutarTransaccion (Singleton).
 * SEGURIDAD: la comprobación de saldo se hace DENTRO del UPDATE
 *           ("saldo - monto >= mínimo"), así dos operaciones simultáneas no
 *           pueden dejar la cuenta por debajo de su límite.
 */
public class TransaccionDAO implements ITransaccionDAO {

    // Consulta base: une la transacción con los números de cuenta y el comprobante
    private static final String SELECT_VISTA = """
        SELECT t.id, t.tipo, t.monto, t.fecha,
               co.numero AS origen, cd.numero AS destino, cp.codigo
        FROM transaccion t
        LEFT JOIN cuenta co ON co.id = t.cuenta_origen_id
        LEFT JOIN cuenta cd ON cd.id = t.cuenta_destino_id
        LEFT JOIN comprobante cp ON cp.transaccion_id = t.id
        """;

    private TransaccionVista mapear(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fecha");
        return new TransaccionVista(
                rs.getInt("id"),
                rs.getString("tipo"),
                rs.getBigDecimal("monto"),
                fecha == null ? null : fecha.toLocalDateTime(),
                rs.getString("origen"),
                rs.getString("destino"),
                rs.getString("codigo"));
    }

    // ---------------- Registro de movimientos ----------------

    @Override
    public TransaccionVista registrarDeposito(int cuentaId, BigDecimal monto) throws SQLException {
        return registrar(TipoTransaccion.DEPOSITO, "DEP", null, cuentaId, monto, null);
    }

    @Override
    public TransaccionVista registrarRetiro(int cuentaId, BigDecimal monto, BigDecimal saldoMinimo)
            throws SQLException {
        return registrar(TipoTransaccion.RETIRO, "RET", cuentaId, null, monto, saldoMinimo);
    }

    @Override
    public TransaccionVista registrarTransferencia(int origenId, int destinoId, BigDecimal monto,
            BigDecimal saldoMinimo) throws SQLException {
        return registrar(TipoTransaccion.TRANSFERENCIA, "TRF", origenId, destinoId, monto, saldoMinimo);
    }

    @Override
    public TransaccionVista registrarPago(int cuentaId, BigDecimal monto, BigDecimal saldoMinimo,
            String codigoServicio) throws SQLException {
        return registrar(TipoTransaccion.PAGO_SERVICIO, "PAG-" + codigoServicio, cuentaId, null,
                monto, saldoMinimo);
    }

    /**
     * Núcleo común. origenId o destinoId pueden ser null según el tipo:
     * depósito (solo destino), retiro y pago (solo origen), transferencia (ambos).
     */
    private TransaccionVista registrar(String tipo, String prefijo, Integer origenId,
            Integer destinoId, BigDecimal monto, BigDecimal saldoMinimo) throws SQLException {

        return ConexionBD.getInstancia().ejecutarTransaccion(con -> {

            if (origenId != null) {
                // La condición "saldo - monto >= mínimo" evita dejar la cuenta bajo su límite
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE cuenta SET saldo = saldo - ? "
                      + "WHERE id = ? AND estado = ? AND saldo - ? >= ?")) {
                    ps.setBigDecimal(1, monto);
                    ps.setInt(2, origenId);
                    ps.setString(3, EstadoCuenta.ACTIVA);
                    ps.setBigDecimal(4, monto);
                    ps.setBigDecimal(5, saldoMinimo);
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException(
                            "Saldo insuficiente o la cuenta de origen no está activa.");
                    }
                }
            }

            if (destinoId != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE cuenta SET saldo = saldo + ? WHERE id = ? AND estado = ?")) {
                    ps.setBigDecimal(1, monto);
                    ps.setInt(2, destinoId);
                    ps.setString(3, EstadoCuenta.ACTIVA);
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException(
                            "La cuenta de destino no existe o no está activa.");
                    }
                }
            }

            int transaccionId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transaccion (cuenta_origen_id, cuenta_destino_id, tipo, monto) "
                  + "VALUES (?, ?, ?, ?)", new String[]{"id"})) {
                asignarEntero(ps, 1, origenId);
                asignarEntero(ps, 2, destinoId);
                ps.setString(3, tipo);
                ps.setBigDecimal(4, monto);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    transaccionId = rs.getInt(1);
                }
            }

            // Comprobante: código corto y único, por ejemplo TRF-000123
            String codigo = prefijo + "-" + String.format("%06d", transaccionId);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO comprobante (transaccion_id, codigo) VALUES (?, ?)")) {
                ps.setInt(1, transaccionId);
                ps.setString(2, codigo);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(SELECT_VISTA + "WHERE t.id = ?")) {
                ps.setInt(1, transaccionId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return mapear(rs);
                }
            }
        });
    }

    private void asignarEntero(PreparedStatement ps, int posicion, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicion, Types.INTEGER);
        } else {
            ps.setInt(posicion, valor);
        }
    }

    // ---------------- Consultas ----------------

    @Override
    public List<TransaccionVista> listarPorCuenta(int cuentaId) throws SQLException {
        return consultar(SELECT_VISTA
                + "WHERE t.cuenta_origen_id = ? OR t.cuenta_destino_id = ? "
                + "ORDER BY t.fecha DESC, t.id DESC LIMIT 50",
                cuentaId, cuentaId);
    }

    @Override
    public List<TransaccionVista> listarPorCliente(int clienteId) throws SQLException {
        return consultar(SELECT_VISTA
                + "WHERE co.cliente_id = ? OR cd.cliente_id = ? "
                + "ORDER BY t.fecha DESC, t.id DESC LIMIT 50",
                clienteId, clienteId);
    }

    @Override
    public List<TransaccionVista> buscar(String tipo, LocalDate desde, LocalDate hasta,
            BigDecimal montoMinimo) throws SQLException {

        StringBuilder sql = new StringBuilder(SELECT_VISTA).append("WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND t.tipo = ?");
            parametros.add(tipo);
        }
        if (desde != null) {
            sql.append(" AND t.fecha >= ?");
            parametros.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hasta != null) {
            sql.append(" AND t.fecha < ?");
            parametros.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        }
        if (montoMinimo != null) {
            sql.append(" AND t.monto >= ?");
            parametros.add(montoMinimo);
        }
        sql.append(" ORDER BY t.fecha DESC, t.id DESC LIMIT 500");

        return consultar(sql.toString(), parametros.toArray());
    }

    @Override
    public TransaccionVista buscarPorComprobante(String codigo) throws SQLException {
        List<TransaccionVista> lista = consultar(SELECT_VISTA + "WHERE cp.codigo = ?", codigo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public BigDecimal totalTransferidoHoy(int cuentaOrigenId) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COALESCE(SUM(monto), 0) FROM transaccion "
              + "WHERE tipo = ? AND cuenta_origen_id = ? AND fecha >= CURRENT_DATE")) {
            ps.setString(1, TipoTransaccion.TRANSFERENCIA);
            ps.setInt(2, cuentaOrigenId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    // Ejecuta una consulta con parámetros y convierte las filas a TransaccionVista
    private List<TransaccionVista> consultar(String sql, Object... parametros) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        List<TransaccionVista> lista = new ArrayList<>();
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
