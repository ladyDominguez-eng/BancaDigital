package com.proyecto.service;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cuenta;
import com.proyecto.util.Validaciones;
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

        Validaciones.montoValido(monto);

        if (numeroDestino == null || numeroDestino.isBlank()) {
            throw new IllegalArgumentException("Indica el número de la cuenta de destino.");
        }

        Cuenta origen = cuentaPropia(cuentaOrigenId, usuarioId);
        Validaciones.cuentaActiva(origen, "de origen");
        Validaciones.saldoSuficiente(origen, monto);

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

        Validaciones.montoValido(monto);
        ServicioPago tipo = ServicioPago.desde(servicio);

        Cuenta cuenta = cuentaPropia(cuentaId, usuarioId);
        Validaciones.cuentaActiva(cuenta, "de pago");
        Validaciones.saldoSuficiente(cuenta, monto);

        return transacciones.registrarPago(
                cuenta.getId(), monto, cuenta.getSaldoMinimo(), tipo.name());
    }

    // Las validaciones comunes (monto, cuenta activa, saldo) están en util.Validaciones
    // y las comparte también el servicio del cajero: no se copian en cada servicio.

    // Una cuenta ajena se trata como inexistente: no se revela que existe
    private Cuenta cuentaPropia(int cuentaId, int usuarioId) throws SQLException {
        Cuenta c = cuentas.buscarDeUsuario(cuentaId, usuarioId);
        if (c == null) {
            throw new IllegalArgumentException("La cuenta seleccionada no existe.");
        }
        return c;
    }

}
