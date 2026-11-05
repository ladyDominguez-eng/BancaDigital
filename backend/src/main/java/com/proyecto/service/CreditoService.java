package com.proyecto.service;

import com.proyecto.dao.ISolicitudCreditoDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cliente;
import com.proyecto.model.HistorialCliente;
import com.proyecto.model.SolicitudCredito;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;
import com.proyecto.patterns.estado.Estados;
import com.proyecto.util.TransicionInvalidaException;
import com.proyecto.util.Validaciones;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Ciclo de vida de las solicitudes de crédito: el cliente las pide (RF-06) y
 * el asesor las evalúa, aprueba o rechaza (RF-07, RF-08, RF-11).
 *
 * Aquí NO hay "if estado == ..." : las transiciones permitidas las decide el
 * patrón State dentro de SolicitudCredito. Este servicio solo carga, pide la
 * acción, guarda y audita (Observer).
 */
@Service
public class CreditoService {

    // Reglas de la solicitud (constantes con nombre, no números sueltos)
    static final BigDecimal MONTO_MINIMO = new BigDecimal("500.00");
    static final BigDecimal MONTO_MAXIMO = new BigDecimal("100000.00");
    static final int PLAZO_MINIMO_MESES = 3;
    static final int PLAZO_MAXIMO_MESES = 60;
    static final int LARGO_MAXIMO_OBSERVACION = 255; // límite de la columna en la base

    static final String PARAM_TASA = "TASA_INTERES_CREDITO";
    static final BigDecimal TASA_POR_DEFECTO = new BigDecimal("18.50");

    private final ISolicitudCreditoDAO solicitudes;
    private final ITransaccionDAO transacciones;
    private final ClienteService clienteService;
    private final ParametroService parametros;
    private final AuditoriaPublisher auditoria;

    public CreditoService(ISolicitudCreditoDAO solicitudes, ITransaccionDAO transacciones,
            ClienteService clienteService, ParametroService parametros,
            AuditoriaPublisher auditoria) {
        this.solicitudes = solicitudes;
        this.transacciones = transacciones;
        this.clienteService = clienteService;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    // ---------------- Cliente ----------------

    public SolicitudCredito solicitar(int usuarioId, BigDecimal monto, int plazoMeses)
            throws SQLException {

        Validaciones.montoValido(monto);
        if (monto.compareTo(MONTO_MINIMO) < 0 || monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new IllegalArgumentException("El monto debe estar entre S/ " + MONTO_MINIMO
                    + " y S/ " + MONTO_MAXIMO + ".");
        }
        if (plazoMeses < PLAZO_MINIMO_MESES || plazoMeses > PLAZO_MAXIMO_MESES) {
            throw new IllegalArgumentException("El plazo debe estar entre " + PLAZO_MINIMO_MESES
                    + " y " + PLAZO_MAXIMO_MESES + " meses.");
        }

        Cliente cliente = clienteService.clienteDe(usuarioId);
        int id = solicitudes.crear(cliente.id(), monto, plazoMeses);
        auditoria.publicar(new EventoAuditoria(usuarioId, "CREDITO_SOLICITADO #" + id));
        return conCuota(solicitudes.buscarPorId(id));
    }

    public List<SolicitudCredito> misSolicitudes(int usuarioId) throws SQLException {
        Cliente cliente = clienteService.clienteDe(usuarioId);
        return conCuotas(solicitudes.listarPorCliente(cliente.id()));
    }

    // ---------------- Asesor ----------------

    public List<SolicitudCredito> listar(String estado) throws SQLException {
        if (estado != null && !estado.isBlank() && !Estados.NOMBRES.contains(estado)) {
            throw new IllegalArgumentException("Estado de solicitud no válido.");
        }
        return conCuotas(solicitudes.listar(estado));
    }

    public SolicitudCredito detalle(int id) throws SQLException {
        return conCuota(cargar(id));
    }

    public SolicitudCredito evaluar(int asesorId, int id) throws SQLException {
        return cambiarEstado(asesorId, id, null, "CREDITO_EN_EVALUACION", SolicitudCredito::evaluar);
    }

    public SolicitudCredito aprobar(int asesorId, int id, String observaciones) throws SQLException {
        return cambiarEstado(asesorId, id, textoOpcional(observaciones), "CREDITO_APROBADO",
                SolicitudCredito::aprobar);
    }

    public SolicitudCredito rechazar(int asesorId, int id, String observaciones) throws SQLException {
        // Un rechazo debe justificarse: el cliente y la auditoría necesitan saber por qué (RF-11)
        if (observaciones == null || observaciones.isBlank()) {
            throw new IllegalArgumentException("Indica el motivo del rechazo en las observaciones.");
        }
        return cambiarEstado(asesorId, id, textoOpcional(observaciones), "CREDITO_RECHAZADO",
                SolicitudCredito::rechazar);
    }

    public SolicitudCredito registrarObservacion(int asesorId, int id, String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Escribe una observación.");
        }
        cargar(id); // comprueba que exista
        solicitudes.actualizarObservaciones(id, asesorId, textoOpcional(texto));
        auditoria.publicar(new EventoAuditoria(asesorId, "CREDITO_OBSERVACION #" + id));
        return conCuota(cargar(id));
    }

    public HistorialCliente historial(int clienteId) throws SQLException {
        return new HistorialCliente(
                conCuotas(solicitudes.listarPorCliente(clienteId)),
                transacciones.listarPorCliente(clienteId));
    }

    // ---------------- Interno ----------------

    /**
     * Patrón State en acción: la acción (evaluar/aprobar/rechazar) la valida
     * el estado actual de la solicitud; si no está permitida lanza
     * TransicionInvalidaException (409) y no se toca la base de datos.
     */
    private SolicitudCredito cambiarEstado(int asesorId, int id, String observaciones,
            String evento, java.util.function.Consumer<SolicitudCredito> accion) throws SQLException {

        SolicitudCredito s = cargar(id);
        String anterior = s.getEstado();

        accion.accept(s); // aquí State decide si se puede, y cambia el estado

        boolean guardado = solicitudes.actualizarEstado(id, anterior, s.getEstado(), asesorId, observaciones);
        if (!guardado) {
            // Otro asesor la modificó entre la lectura y el guardado
            throw new TransicionInvalidaException(
                "La solicitud cambió de estado mientras la revisabas. Recárgala e inténtalo de nuevo.");
        }

        auditoria.publicar(new EventoAuditoria(asesorId, evento + " #" + id));
        return conCuota(cargar(id));
    }

    private SolicitudCredito cargar(int id) throws SQLException {
        SolicitudCredito s = solicitudes.buscarPorId(id);
        if (s == null) {
            throw new IllegalArgumentException("La solicitud no existe.");
        }
        return s;
    }

    // Recorta espacios y respeta el largo máximo de la columna
    private String textoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.trim();
        if (limpio.length() > LARGO_MAXIMO_OBSERVACION) {
            throw new IllegalArgumentException(
                "La observación admite como máximo " + LARGO_MAXIMO_OBSERVACION + " caracteres.");
        }
        return limpio;
    }

    // GRASP Experto: la solicitud calcula su cuota; el servicio solo le da la tasa vigente
    private SolicitudCredito conCuota(SolicitudCredito s) throws SQLException {
        s.setCuotaMensual(s.calcularCuota(parametros.obtenerNumero(PARAM_TASA, TASA_POR_DEFECTO)));
        return s;
    }

    private List<SolicitudCredito> conCuotas(List<SolicitudCredito> lista) throws SQLException {
        BigDecimal tasa = parametros.obtenerNumero(PARAM_TASA, TASA_POR_DEFECTO);
        for (SolicitudCredito s : lista) {
            s.setCuotaMensual(s.calcularCuota(tasa));
        }
        return lista;
    }
}
