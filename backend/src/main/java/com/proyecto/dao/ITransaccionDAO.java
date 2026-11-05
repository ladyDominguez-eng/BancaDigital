package com.proyecto.dao;

import com.proyecto.model.TransaccionVista;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de los movimientos de dinero y su consulta.
 *
 * PRINCIPIO: S de SOLID. CuentaDAO solo lee cuentas; todo lo que mueve dinero
 * y deja transacción + comprobante vive aquí. D de SOLID: los servicios
 * dependen de esta interfaz, no de JDBC.
 *
 * Cada método de registro es ATÓMICO (RNF-03): actualiza saldos, guarda la
 * transacción y su comprobante juntos, o no hace nada (rollback).
 */
public interface ITransaccionDAO {

    TransaccionVista registrarDeposito(int cuentaId, BigDecimal monto) throws SQLException;

    /** saldoMinimo: hasta dónde puede bajar la cuenta (0, o negativo si tiene sobregiro). */
    TransaccionVista registrarRetiro(int cuentaId, BigDecimal monto, BigDecimal saldoMinimo)
            throws SQLException;

    TransaccionVista registrarTransferencia(int origenId, int destinoId, BigDecimal monto,
            BigDecimal saldoMinimo) throws SQLException;

    /** codigoServicio se incluye en el comprobante (por ejemplo LUZ). */
    TransaccionVista registrarPago(int cuentaId, BigDecimal monto, BigDecimal saldoMinimo,
            String codigoServicio) throws SQLException;

    /** Movimientos en los que participa una cuenta, del más reciente al más antiguo. */
    List<TransaccionVista> listarPorCuenta(int cuentaId) throws SQLException;

    /** Movimientos de todas las cuentas de un cliente. */
    List<TransaccionVista> listarPorCliente(int clienteId) throws SQLException;

    /** Búsqueda con filtros opcionales (cualquiera puede ser null). */
    List<TransaccionVista> buscar(String tipo, LocalDate desde, LocalDate hasta,
            BigDecimal montoMinimo) throws SQLException;

    /** Transacción por código de comprobante, o null si no existe. */
    TransaccionVista buscarPorComprobante(String codigo) throws SQLException;

    /** Suma de transferencias hechas hoy desde una cuenta (para el límite diario). */
    BigDecimal totalTransferidoHoy(int cuentaOrigenId) throws SQLException;
}
