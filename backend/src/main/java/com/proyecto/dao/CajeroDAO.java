package com.proyecto.dao;

import com.proyecto.model.Movimiento;
import com.proyecto.model.ResultadoOperacionCajero;
import com.proyecto.util.ConexionBD;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class CajeroDAO {

    private final Connection conexion;

    public CajeroDAO() {
        this.conexion = ConexionBD.getInstancia().getConexion();
    }

    public ResultadoOperacionCajero ejecutarOperacion(
            Movimiento movimiento,
            String tipo) throws SQLException {

        boolean autoCommitAnterior = conexion.getAutoCommit();

        try {

            conexion.setAutoCommit(false);

            // 1. Obtener la cuenta y bloquearla durante la operación
            String sqlCuenta = """
                SELECT saldo, estado
                FROM cuenta
                WHERE id = ?
                FOR UPDATE
                """;

            BigDecimal saldoActual;
            String estado;

            try (PreparedStatement ps = conexion.prepareStatement(sqlCuenta)) {

                ps.setLong(1, movimiento.getCuentaId());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        conexion.rollback();

                        return new ResultadoOperacionCajero(
                                false,
                                "La cuenta no existe.",
                                null
                        );
                    }

                    saldoActual = rs.getBigDecimal("saldo");
                    estado = rs.getString("estado");
                }
            }

            // 2. Verificar estado de la cuenta
            if (!"ACTIVA".equalsIgnoreCase(estado)) {

                conexion.rollback();

                return new ResultadoOperacionCajero(
                        false,
                        "La cuenta no está activa.",
                        saldoActual
                );
            }

            // 3. Calcular nuevo saldo
            BigDecimal nuevoSaldo;

            if ("DEPOSITO".equals(tipo)) {

                nuevoSaldo = saldoActual.add(movimiento.getMonto());

            } else {

                if (saldoActual.compareTo(movimiento.getMonto()) < 0) {

                    conexion.rollback();

                    return new ResultadoOperacionCajero(
                            false,
                            "Saldo insuficiente para realizar el retiro.",
                            saldoActual
                    );
                }

                nuevoSaldo = saldoActual.subtract(movimiento.getMonto());
            }

            // 4. Actualizar saldo de la cuenta
            String sqlActualizar = """
                UPDATE cuenta
                SET saldo = ?
                WHERE id = ?
                """;

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlActualizar)) {

                ps.setBigDecimal(1, nuevoSaldo);
                ps.setLong(2, movimiento.getCuentaId());

                ps.executeUpdate();
            }

            // 5. Registrar la transacción
            String sqlTransaccion = """
                INSERT INTO transaccion
                (
                    cuenta_id,
                    tipo,
                    monto,
                    saldo_resultante,
                    descripcion,
                    usuario_id
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlTransaccion)) {

                ps.setLong(1, movimiento.getCuentaId());
                ps.setString(2, tipo);
                ps.setBigDecimal(3, movimiento.getMonto());
                ps.setBigDecimal(4, nuevoSaldo);
                ps.setString(5, movimiento.getDescripcion());

                if (movimiento.getUsuarioId() != null) {
                    ps.setLong(6, movimiento.getUsuarioId());
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }

                ps.executeUpdate();
            }

            // 6. Confirmar toda la operación
            conexion.commit();

            return new ResultadoOperacionCajero(
                    true,
                    "Operación realizada correctamente.",
                    nuevoSaldo
            );

        } catch (SQLException e) {

            try {
                conexion.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            throw e;

        } finally {

            try {
                conexion.setAutoCommit(autoCommitAnterior);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
