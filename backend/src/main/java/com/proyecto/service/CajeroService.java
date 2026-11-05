package com.proyecto.service;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cuenta;
import com.proyecto.model.TransaccionVista;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;
import com.proyecto.util.Validaciones;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Reglas del cajero: consultar cuentas (RF-10), registrar depósitos y
 * retiros (RF-09) y consultar comprobantes (RF-14).
 *
 * D de SOLID: depende de interfaces DAO que recibe por constructor (antes
 * las creaba con la Factory dentro de la clase). Observer: cada operación
 * se publica a la auditoría, con el id del cajero que la hizo.
 */
@Service
public class CajeroService {

    // LÍMITES: el administrador los configura en parametro_sistema (RF-03)
    // con estos nombres. PROBLEMA: antes el 50 000 estaba fijo en el código
    // y cambiarlo exigía recompilar y desplegar. Si el parámetro no existe
    // se usa el valor por defecto, así nada se rompe.
    static final String PARAM_LIMITE_DEPOSITO = "LIMITE_DEPOSITO";
    static final BigDecimal LIMITE_DEPOSITO_POR_DEFECTO = new BigDecimal("50000.00");
    static final String PARAM_LIMITE_RETIRO = "LIMITE_RETIRO";
    static final BigDecimal LIMITE_RETIRO_POR_DEFECTO = new BigDecimal("10000.00");

    private final ICuentaDAO cuentas;
    private final ITransaccionDAO transacciones;
    private final ParametroService parametros;
    private final AuditoriaPublisher auditoria;

    public CajeroService(ICuentaDAO cuentas, ITransaccionDAO transacciones,
            ParametroService parametros, AuditoriaPublisher auditoria) {
        this.cuentas = cuentas;
        this.transacciones = transacciones;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    // CONSULTAR: busca por número de cuenta o por DNI
    public List<Cuenta> consultar(String dni, String numero) throws SQLException {

        if (numero != null && !numero.isBlank()) {
            Cuenta c = cuentas.buscarPorNumero(numero.trim());
            List<Cuenta> lista = new ArrayList<>();
            if (c != null) lista.add(c);
            return lista;
        }

        if (dni != null && !dni.isBlank()) {
            return cuentas.buscarPorDni(dni.trim());
        }

        throw new IllegalArgumentException("Ingresa un DNI o un número de cuenta.");
    }

    public TransaccionVista depositar(int cajeroId, String numero, BigDecimal monto)
            throws SQLException {

        Validaciones.montoValido(monto);
        BigDecimal limite = parametros.obtenerNumero(PARAM_LIMITE_DEPOSITO, LIMITE_DEPOSITO_POR_DEFECTO);
        if (monto.compareTo(limite) > 0) {
            throw new IllegalArgumentException("El monto supera el máximo permitido.");
        }

        Cuenta c = cuentaExistente(numero);
        Validaciones.cuentaActiva(c, "");

        TransaccionVista t = transacciones.registrarDeposito(c.getId(), monto);
        auditoria.publicar(new EventoAuditoria(cajeroId, "DEPOSITO " + t.comprobante()));
        return t;
    }

    public TransaccionVista retirar(int cajeroId, String numero, BigDecimal monto)
            throws SQLException {

        Validaciones.montoValido(monto);
        BigDecimal limite = parametros.obtenerNumero(PARAM_LIMITE_RETIRO, LIMITE_RETIRO_POR_DEFECTO);
        if (monto.compareTo(limite) > 0) {
            throw new IllegalArgumentException("El monto supera el máximo de retiro permitido.");
        }

        Cuenta c = cuentaExistente(numero);
        Validaciones.cuentaActiva(c, "");
        Validaciones.saldoSuficiente(c, monto);

        // El saldo mínimo lo define la propia cuenta (Experto): 0 en ahorro, sobregiro en corriente
        TransaccionVista t = transacciones.registrarRetiro(c.getId(), monto, c.getSaldoMinimo());
        auditoria.publicar(new EventoAuditoria(cajeroId, "RETIRO " + t.comprobante()));
        return t;
    }

    /** Busca un comprobante por su código (RF-14). */
    public TransaccionVista buscarComprobante(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Ingresa el código del comprobante.");
        }
        TransaccionVista t = transacciones.buscarPorComprobante(codigo.trim().toUpperCase());
        if (t == null) {
            throw new IllegalArgumentException("No existe un comprobante con ese código.");
        }
        return t;
    }

    private Cuenta cuentaExistente(String numero) throws SQLException {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de cuenta es obligatorio.");
        }
        Cuenta c = cuentas.buscarPorNumero(numero.trim());
        if (c == null) {
            throw new IllegalArgumentException("La cuenta no existe.");
        }
        return c;
    }
}
