package com.proyecto.service;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cuenta;
import com.proyecto.model.EstadoCuenta;
import com.proyecto.model.ServicioPago;
import com.proyecto.model.TransaccionVista;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.springframework.stereotype.Service;

/**
 * Reglas de negocio de las operaciones del cliente que mueven dinero:
 * transferir (RF-05) y pagar servicios.
 *
 * CAPAS: validar y decidir es trabajo de esta capa (Service); guardar es del
 * DAO. Por eso aquí no hay SQL, y el DAO no valida reglas del banco.
 *
 * GRASP Experto: quien sabe cuánto puede bajar una cuenta es la propia
 * Cuenta (getSaldoMinimo), no este servicio con un "if" por tipo.
 */
@Service
public class TransferenciaService {

    // Parámetros editables por el administrador (RF-03) y su valor por defecto
    static final String PARAM_LIMITE_DIARIO = "LIMITE_TRANSFERENCIA_DIARIA";
    static final BigDecimal LIMITE_DIARIO_POR_DEFECTO = new BigDecimal("5000.00");

    private final ICuentaDAO cuentas;
    private final ITransaccionDAO transacciones;
    private final ParametroService parametros;

    public TransferenciaService(ICuentaDAO cuentas, ITransaccionDAO transacciones,
            ParametroService parametros) {
        this.cuentas = cuentas;
        this.transacciones = transacciones;
        this.parametros = parametros;
    }

    public TransaccionVista transferir(int usuarioId, int cuentaOrigenId, String numeroDestino,
            BigDecimal monto) throws SQLException {

        validarMonto(monto);

        if (numeroDestino == null || numeroDestino.isBlank()) {
            throw new IllegalArgumentException("Indica el número de la cuenta de destino.");
        }

        Cuenta origen = cuentaPropia(cuentaOrigenId, usuarioId);
        validarActiva(origen, "origen");
        validarSaldo(origen, monto);

        Cuenta destino = cuentas.buscarPorNumero(numeroDestino.trim());
        if (destino == null) {
            throw new IllegalArgumentException("La cuenta de destino no existe.");
        }
        if (destino.getId() == origen.getId()) {
            throw new IllegalArgumentException("No puedes transferir a la misma cuenta.");
        }

        // Límite diario configurable (RF-03)
        BigDecimal limite = parametros.obtenerNumero(PARAM_LIMITE_DIARIO, LIMITE_DIARIO_POR_DEFECTO);
        BigDecimal yaTransferido = transacciones.totalTransferidoHoy(origen.getId());
        if (yaTransferido.add(monto).compareTo(limite) > 0) {
            throw new IllegalArgumentException(
                "Supera el límite diario de transferencias (S/ " + limite
                + "). Hoy ya transferiste S/ " + yaTransferido + ".");
        }

        return transacciones.registrarTransferencia(
                origen.getId(), destino.getId(), monto, origen.getSaldoMinimo());
    }

    public TransaccionVista pagarServicio(int usuarioId, int cuentaId, String servicio,
            BigDecimal monto) throws SQLException {

        validarMonto(monto);
        ServicioPago tipo = ServicioPago.desde(servicio);

        Cuenta cuenta = cuentaPropia(cuentaId, usuarioId);
        validarActiva(cuenta, "de pago");
        validarSaldo(cuenta, monto);

        return transacciones.registrarPago(
                cuenta.getId(), monto, cuenta.getSaldoMinimo(), tipo.name());
    }

    // ---------------- Validaciones reutilizadas ----------------

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        if (monto.scale() > 2) {
            throw new IllegalArgumentException("El monto admite como máximo 2 decimales.");
        }
    }

    // Una cuenta ajena se trata como inexistente: no se revela que existe
    private Cuenta cuentaPropia(int cuentaId, int usuarioId) throws SQLException {
        Cuenta c = cuentas.buscarDeUsuario(cuentaId, usuarioId);
        if (c == null) {
            throw new IllegalArgumentException("La cuenta seleccionada no existe.");
        }
        return c;
    }

    private void validarActiva(Cuenta c, String rol) {
        if (!EstadoCuenta.ACTIVA.equals(c.getEstado())) {
            throw new IllegalArgumentException("La cuenta " + rol + " no está activa.");
        }
    }

    private void validarSaldo(Cuenta c, BigDecimal monto) {
        if (c.getSaldo().subtract(monto).compareTo(c.getSaldoMinimo()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }
}
