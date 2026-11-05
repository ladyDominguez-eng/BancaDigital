package com.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.IParametroDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cuenta;
import com.proyecto.model.CuentaAhorro;
import com.proyecto.model.CuentaCorriente;
import com.proyecto.model.TransaccionVista;
import com.proyecto.patterns.AuditoriaPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del servicio del cajero con DAO simulados (sin base de datos). */
class CajeroServiceTest {

    private ICuentaDAO cuentas;
    private ITransaccionDAO transacciones;
    private AuditoriaPublisher auditoria;
    private CajeroService servicio;

    private static final int CAJERO = 5;

    @BeforeEach
    void preparar() throws Exception {
        cuentas = mock(ICuentaDAO.class);
        transacciones = mock(ITransaccionDAO.class);
        auditoria = mock(AuditoriaPublisher.class);
        IParametroDAO parametroDAO = mock(IParametroDAO.class);
        when(parametroDAO.buscarPorNombre(any())).thenReturn(null); // límites por defecto
        servicio = new CajeroService(cuentas, transacciones, new ParametroService(parametroDAO), auditoria);
    }

    private Cuenta cuenta(Cuenta c, String saldo, String estado) {
        c.setId(1);
        c.setNumero("001-1");
        c.setSaldo(new BigDecimal(saldo));
        c.setEstado(estado);
        return c;
    }

    private TransaccionVista vista(String tipo, String codigo) {
        return new TransaccionVista(9, tipo, BigDecimal.TEN, LocalDateTime.now(), null, "001-1", codigo);
    }

    @Test
    void depositoCorrectoEmiteComprobanteYAudita() throws Exception {
        when(cuentas.buscarPorNumero("001-1")).thenReturn(cuenta(new CuentaAhorro(), "10.00", "ACTIVA"));
        when(transacciones.registrarDeposito(eq(1), any())).thenReturn(vista("DEPOSITO", "DEP-000009"));

        TransaccionVista t = servicio.depositar(CAJERO, "001-1", new BigDecimal("25.50"));

        assertEquals("DEP-000009", t.comprobante());
        verify(auditoria).publicar(any());
    }

    @Test
    void depositoSobreElLimiteSeRechaza() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.depositar(CAJERO, "001-1", new BigDecimal("50000.01")));
        verify(transacciones, never()).registrarDeposito(anyInt(), any());
    }

    @Test
    void noSeDepositaEnUnaCuentaBloqueada() throws Exception {
        when(cuentas.buscarPorNumero("001-1")).thenReturn(cuenta(new CuentaAhorro(), "10.00", "BLOQUEADA"));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.depositar(CAJERO, "001-1", BigDecimal.TEN));
    }

    @Test
    void retiroSinSaldoSeRechaza() throws Exception {
        when(cuentas.buscarPorNumero("001-1")).thenReturn(cuenta(new CuentaAhorro(), "50.00", "ACTIVA"));

        var e = assertThrows(IllegalArgumentException.class,
                () -> servicio.retirar(CAJERO, "001-1", new BigDecimal("50.01")));

        assertEquals("Saldo insuficiente.", e.getMessage());
        verify(transacciones, never()).registrarRetiro(anyInt(), any(), any());
    }

    @Test
    void retiroEnCorrientePuedeUsarElSobregiro() throws Exception {
        when(cuentas.buscarPorNumero("001-1")).thenReturn(cuenta(new CuentaCorriente(), "50.00", "ACTIVA"));
        when(transacciones.registrarRetiro(anyInt(), any(), any())).thenReturn(vista("RETIRO", "RET-000009"));

        servicio.retirar(CAJERO, "001-1", new BigDecimal("500.00")); // 50 + 500 de sobregiro

        verify(transacciones).registrarRetiro(eq(1), eq(new BigDecimal("500.00")), eq(new BigDecimal("-500.00")));
    }

    @Test
    void retiroSobreElLimiteSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.retirar(CAJERO, "001-1", new BigDecimal("10000.01")));
    }

    @Test
    void comprobanteInexistenteDaError() throws Exception {
        when(transacciones.buscarPorComprobante("XXX")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> servicio.buscarComprobante("xxx"));
    }
}
