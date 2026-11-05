package com.proyecto.service;

import com.proyecto.dao.IRegistroClienteDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.dao.factory.CuentaFactory;
import com.proyecto.model.ClienteRegistrado;
import com.proyecto.model.RegistrarClienteRequest;
import com.proyecto.model.TransaccionVista;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;
import com.proyecto.util.Validaciones;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Alta de un cliente nuevo: usuario de acceso + datos personales + primera
 * cuenta, todo desde la pantalla del Administrador.
 *
 * PROBLEMA: crear un cliente exigía escribir SQL a mano en la base: había
 *           que insertar en tres tablas, inventar el número de cuenta y
 *           calcular el hash de la contraseña. Fácil de equivocarse y
 *           imposible de explicar como funcionalidad del sistema.
 * SOLUCIÓN: este servicio valida los datos (GRASP Experto), pide a
 *           IRegistroClienteDAO crear todo en UNA transacción y, si hay
 *           saldo inicial, lo registra como un depósito normal para que
 *           quede comprobante y rastro en la auditoría.
 *
 * PATRONES Y PRINCIPIOS
 * - Factory: CuentaFactory decide qué tipos de cuenta existen; este servicio
 *   no tiene un "if AHORRO / CORRIENTE" (O de SOLID).
 * - Observer: el alta se publica en la auditoría.
 * - D de SOLID: depende de interfaces DAO inyectadas por constructor.
 * - S de SOLID: no reemplaza a UsuarioService (que gestiona el personal);
 *   una clase por responsabilidad.
 * POR QUÉ NO OTRO PATRÓN: Builder serviría para armar un objeto de muchos
 *   campos opcionales; aquí todos los datos son obligatorios y viajan en un
 *   record. Template Method sobraría: solo hay un flujo de alta.
 */
@Service
public class RegistroClienteService {

    private final IRegistroClienteDAO registro;
    private final IUsuarioDAO usuarios;
    private final ITransaccionDAO transacciones;
    private final ParametroService parametros;
    private final AuditoriaPublisher auditoria;
    private final Function<String, String> codificador;

    // Constructor que usa Spring: la contraseña se guarda con BCrypt (mismo costo que el login)
    @Autowired
    public RegistroClienteService(IRegistroClienteDAO registro, IUsuarioDAO usuarios,
            ITransaccionDAO transacciones, ParametroService parametros,
            AuditoriaPublisher auditoria) {
        this(registro, usuarios, transacciones, parametros, auditoria,
                new BCryptPasswordEncoder(UsuarioService.COSTO_BCRYPT)::encode);
    }

    // Constructor para pruebas: el codificador se puede simular
    RegistroClienteService(IRegistroClienteDAO registro, IUsuarioDAO usuarios,
            ITransaccionDAO transacciones, ParametroService parametros,
            AuditoriaPublisher auditoria, Function<String, String> codificador) {
        this.registro = registro;
        this.usuarios = usuarios;
        this.transacciones = transacciones;
        this.parametros = parametros;
        this.auditoria = auditoria;
        this.codificador = codificador;
    }

    public ClienteRegistrado registrar(int adminId, RegistrarClienteRequest r) throws SQLException {
        if (r == null) {
            throw new IllegalArgumentException("Faltan los datos del cliente.");
        }

        // 1) Validaciones de formato (mensajes claros para la persona que registra)
        Validaciones.usernameValido(r.username());
        Validaciones.claveValida(r.password());
        Validaciones.dniValido(r.dni());
        Validaciones.nombreValido(r.nombres(), "Los nombres");
        Validaciones.nombreValido(r.apellidos(), "Los apellidos");
        if (!CuentaFactory.esTipoValido(r.tipoCuenta())) {
            throw new IllegalArgumentException("El tipo de cuenta no es válido.");
        }
        BigDecimal saldoInicial = saldoInicialValido(r.saldoInicial());

        // 2) Duplicados: se avisan antes de tocar la base
        if (usuarios.buscarPorUsername(r.username()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
        if (registro.existeDni(r.dni())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese DNI.");
        }

        // 3) Alta atómica de usuario + cliente + cuenta (saldo cero)
        String tipo = CuentaFactory.crear(r.tipoCuenta()).getTipo();
        IRegistroClienteDAO.Alta alta = registro.registrar(
                r.username().trim(), codificador.apply(r.password()), r.dni(),
                r.nombres().trim(), r.apellidos().trim(), tipo);

        // 4) Saldo inicial como un depósito normal: deja comprobante y movimiento
        String comprobante = null;
        if (saldoInicial.signum() > 0) {
            try {
                TransaccionVista t = transacciones.registrarDeposito(alta.cuentaId(), saldoInicial);
                comprobante = t.comprobante();
            } catch (SQLException e) {
                throw new IllegalArgumentException("El cliente quedó registrado con la cuenta "
                        + alta.numeroCuenta() + ", pero no se pudo registrar el depósito inicial. "
                        + "Regístralo desde el rol Cajero.");
            }
        }

        auditoria.publicar(new EventoAuditoria(adminId,
                "REGISTRO_CLIENTE " + r.username().trim() + " CUENTA " + alta.numeroCuenta()));

        return new ClienteRegistrado(alta.usuarioId(), alta.clienteId(), alta.cuentaId(),
                alta.numeroCuenta(), comprobante);
    }

    // El saldo inicial es opcional; si viene, debe cumplir las mismas reglas
    // que cualquier depósito (monto válido y por debajo del límite configurado).
    private BigDecimal saldoInicialValido(BigDecimal saldo) throws SQLException {
        if (saldo == null || saldo.signum() == 0) {
            return BigDecimal.ZERO;
        }
        Validaciones.montoValido(saldo);
        BigDecimal limite = parametros.obtenerNumero(
                CajeroService.PARAM_LIMITE_DEPOSITO, CajeroService.LIMITE_DEPOSITO_POR_DEFECTO);
        if (saldo.compareTo(limite) > 0) {
            throw new IllegalArgumentException("El saldo inicial supera el máximo permitido por depósito.");
        }
        return saldo;
    }
}
