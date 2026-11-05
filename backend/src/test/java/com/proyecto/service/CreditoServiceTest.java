package com.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.proyecto.dao.IClienteDAO;
import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.IParametroDAO;
import com.proyecto.dao.ISolicitudCreditoDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cliente;
import com.proyecto.model.SolicitudCredito;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.util.TransicionInvalidaException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del servicio de créditos con DAO simulados (sin base de datos). */
class CreditoServiceTest {

    private ISolicitudCreditoDAO solicitudes;
    private IClienteDAO clientes;
    private AuditoriaPublisher auditoria;
    private CreditoService servicio;

    private static final int ASESOR = 3;

    @BeforeEach
    void preparar() throws Exception {
        solicitudes = mock(ISolicitudCreditoDAO.class);
        clientes = mock(IClienteDAO.class);
        auditoria = mock(AuditoriaPublisher.class);
        IParametroDAO parametroDAO = mock(IParametroDAO.class);
        when(parametroDAO.buscarPorNombre(any())).thenReturn(null); // tasa por defecto

        ClienteService clienteService =
                new ClienteService(mock(ICuentaDAO.class), mock(ITransaccionDAO.class), clientes);
        servicio = new CreditoService(solicitudes, mock(ITransaccionDAO.class), clienteService,
                new ParametroService(parametroDAO), auditoria);
    }

    private SolicitudCredito solicitud(String estado) {
        SolicitudCredito s = new SolicitudCredito();
        s.setId(7);
        s.setMonto(new BigDecimal("5000.00"));
        s.setPlazoMeses(12);
        s.setEstado(estado);
        return s;
    }

    @Test
    void evaluarPasaDePendienteAEnEvaluacionYGuardaConElEstadoAnterior() throws Exception {
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("PENDIENTE"));
        when(solicitudes.actualizarEstado(anyInt(), any(), any(), any(), any())).thenReturn(true);

        servicio.evaluar(ASESOR, 7);

        verify(solicitudes).actualizarEstado(7, "PENDIENTE", "EN_EVALUACION", ASESOR, null);
        verify(auditoria).publicar(any());
    }

    @Test
    void noSeApruebaUnaSolicitudSinEvaluar() throws Exception {
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("PENDIENTE"));

        assertThrows(TransicionInvalidaException.class, () -> servicio.aprobar(ASESOR, 7, "ok"));

        // State rechazó la acción ANTES de tocar la base de datos
        verify(solicitudes, never()).actualizarEstado(anyInt(), any(), any(), any(), any());
    }

    @Test
    void aprobarUnaSolicitudEnEvaluacionGuardaLaObservacion() throws Exception {
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("EN_EVALUACION"));
        when(solicitudes.actualizarEstado(anyInt(), any(), any(), any(), any())).thenReturn(true);

        servicio.aprobar(ASESOR, 7, "  Buen historial  ");

        verify(solicitudes).actualizarEstado(7, "EN_EVALUACION", "APROBADO", ASESOR, "Buen historial");
    }

    @Test
    void elRechazoExigeMotivo() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> servicio.rechazar(ASESOR, 7, "  "));
        assertThrows(IllegalArgumentException.class, () -> servicio.rechazar(ASESOR, 7, null));
        verifyNoInteractions(solicitudes);
    }

    @Test
    void unaSolicitudResueltaNoCambia() throws Exception {
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("APROBADO"));

        assertThrows(TransicionInvalidaException.class, () -> servicio.rechazar(ASESOR, 7, "motivo"));
        assertThrows(TransicionInvalidaException.class, () -> servicio.evaluar(ASESOR, 7));
    }

    @Test
    void siOtroAsesorLaCambioPrimeroSeInformaElConflicto() throws Exception {
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("PENDIENTE"));
        when(solicitudes.actualizarEstado(anyInt(), any(), any(), any(), any())).thenReturn(false);

        assertThrows(TransicionInvalidaException.class, () -> servicio.evaluar(ASESOR, 7));
        verify(auditoria, never()).publicar(any());
    }

    @Test
    void laSolicitudDevuelveSuCuotaMensual() throws Exception {
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("PENDIENTE"));

        SolicitudCredito s = servicio.detalle(7);

        assertNotNull(s.getCuotaMensual());
        assertTrue(s.getCuotaMensual().signum() > 0);
    }

    @Test
    void solicitarValidaMontoYPlazo() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> servicio.solicitar(1, new BigDecimal("499.99"), 12));
        assertThrows(IllegalArgumentException.class, () -> servicio.solicitar(1, new BigDecimal("100000.01"), 12));
        assertThrows(IllegalArgumentException.class, () -> servicio.solicitar(1, new BigDecimal("1000"), 2));
        assertThrows(IllegalArgumentException.class, () -> servicio.solicitar(1, new BigDecimal("1000"), 61));
        verifyNoInteractions(solicitudes);
    }

    @Test
    void solicitarCreaLaSolicitudDelClienteDelUsuario() throws Exception {
        when(clientes.buscarPorUsuarioId(1)).thenReturn(new Cliente(4, 1, "70123456", "Ana", "Pérez"));
        when(solicitudes.crear(eq(4), any(), eq(12))).thenReturn(7);
        when(solicitudes.buscarPorId(7)).thenReturn(solicitud("PENDIENTE"));

        SolicitudCredito s = servicio.solicitar(1, new BigDecimal("5000.00"), 12);

        assertEquals("PENDIENTE", s.getEstado());
        verify(solicitudes).crear(4, new BigDecimal("5000.00"), 12);
    }

    @Test
    void filtroDeEstadoInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> servicio.listar("INVENTADO"));
    }
}
