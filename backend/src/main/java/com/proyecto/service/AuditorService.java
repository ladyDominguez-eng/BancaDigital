package com.proyecto.service;

import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.ArchivoExportado;
import com.proyecto.model.TipoTransaccion;
import com.proyecto.model.TransaccionVista;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;
import com.proyecto.patterns.exportacion.ExportadorTransacciones;
import com.proyecto.patterns.exportacion.Exportadores;
import com.proyecto.patterns.riesgo.AlertaRiesgo;
import com.proyecto.patterns.riesgo.MontoAltoRegla;
import com.proyecto.patterns.riesgo.OperacionesFrecuentesRegla;
import com.proyecto.patterns.riesgo.ReglaRiesgo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Funciones del auditor: revisar transacciones (RF-12), reporte de riesgo y
 * exportar información (RF-15). La bitácora de accesos ya la sirve
 * /api/auditoria, que también acepta el rol AUDITOR.
 *
 * Strategy: el reporte de riesgo aplica una lista de ReglaRiesgo y la
 * exportación elige un ExportadorTransacciones; este servicio no conoce los
 * detalles de ninguno.
 */
@Service
public class AuditorService {

    // Umbrales configurables por el administrador (RF-03), con valor por defecto
    static final String PARAM_UMBRAL_MONTO = "UMBRAL_RIESGO_MONTO";
    static final BigDecimal UMBRAL_MONTO_POR_DEFECTO = new BigDecimal("3000.00");
    static final String PARAM_MAX_OPERACIONES = "UMBRAL_RIESGO_OPERACIONES_DIA";
    static final BigDecimal MAX_OPERACIONES_POR_DEFECTO = new BigDecimal("5");

    private final ITransaccionDAO transacciones;
    private final ParametroService parametros;
    private final AuditoriaPublisher auditoria;

    public AuditorService(ITransaccionDAO transacciones, ParametroService parametros,
            AuditoriaPublisher auditoria) {
        this.transacciones = transacciones;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    public List<TransaccionVista> revisar(String tipo, LocalDate desde, LocalDate hasta,
            BigDecimal montoMinimo) throws SQLException {
        validarTipo(tipo);
        validarRango(desde, hasta);
        return transacciones.buscar(tipo, desde, hasta, montoMinimo);
    }

    /** Aplica todas las reglas de riesgo a las transacciones del período. */
    public List<AlertaRiesgo> reporteRiesgo(LocalDate desde, LocalDate hasta) throws SQLException {
        validarRango(desde, hasta);
        List<TransaccionVista> lista = transacciones.buscar(null, desde, hasta, null);

        List<AlertaRiesgo> alertas = new ArrayList<>();
        for (ReglaRiesgo regla : reglasVigentes()) {
            alertas.addAll(regla.evaluar(lista));
        }
        alertas.sort(Comparator.comparing(AlertaRiesgo::fecha, Comparator.nullsLast(Comparator.reverseOrder())));
        return alertas;
    }

    public ArchivoExportado exportar(int auditorId, String formato, String tipo, LocalDate desde,
            LocalDate hasta, BigDecimal montoMinimo) throws SQLException {

        ExportadorTransacciones exportador = Exportadores.para(formato); // Strategy elegida
        List<TransaccionVista> lista = revisar(tipo, desde, hasta, montoMinimo);

        // Quedan registradas las exportaciones: son datos sensibles y el auditor también se audita
        auditoria.publicar(new EventoAuditoria(auditorId,
                "EXPORTO_TRANSACCIONES " + exportador.formato() + " (" + lista.size() + ")"));

        return new ArchivoExportado(
                "transacciones-" + LocalDate.now() + "." + exportador.extension(),
                exportador.tipoContenido(),
                exportador.exportar(lista));
    }

    // Las reglas se arman con los umbrales vigentes; para agregar una regla nueva se añade aquí
    private List<ReglaRiesgo> reglasVigentes() throws SQLException {
        BigDecimal umbral = parametros.obtenerNumero(PARAM_UMBRAL_MONTO, UMBRAL_MONTO_POR_DEFECTO);
        int maximo = parametros.obtenerNumero(PARAM_MAX_OPERACIONES, MAX_OPERACIONES_POR_DEFECTO).intValue();
        return List.of(new MontoAltoRegla(umbral), new OperacionesFrecuentesRegla(maximo));
    }

    private void validarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return;
        }
        List<String> validos = List.of(TipoTransaccion.DEPOSITO, TipoTransaccion.RETIRO,
                TipoTransaccion.TRANSFERENCIA, TipoTransaccion.PAGO_SERVICIO);
        if (!validos.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de transacción no válido.");
        }
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
    }
}
