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
import com.proyecto.model.Parametro;
import com.proyecto.model.TransaccionVista;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas de transferencia y pago, sin base de datos: los DAO
 * son simulados. Esto es posible porque el servicio depende de interfaces
 * (D de SOLID).
 */
class TransferenciaServiceTest {

    private ICuentaDAO cuentas;
    private ITransaccionDAO transacciones;
    private IParametroDAO parametroDAO;
    private TransferenciaService servicio;

    private static final int USUARIO = 10;

    @BeforeEach
    void preparar() throws Exception {
        cuentas = mock(ICuentaDAO.class);
        transacciones = mock(ITransaccionDAO.class);
        parametroDAO = mock(IParametroDAO.class);
        servicio = new TransferenciaService(cuentas, transacciones, new ParametroService(parametroDAO));

        // Por defecto: sin límite configurado (se usa 5000) y nada transferido hoy
        when(parametroDAO.buscarPorNombre(any())).thenReturn(null);
        when(transacciones.totalTransferidoHoy(anyInt())).thenReturn(BigDecimal.ZERO);
    }

    private Cuenta cuenta(Cuenta c, int id, String numero, String saldo) {
        c.setId(id);
        c.setNumero(numero);
        c.setSaldo(new BigDecimal(saldo));
        c.setEstado("ACTIVA");
        return c;
    }

    private TransaccionVista vista() {
        return new TransaccionVista(1, "TRANSFERENCIA", BigDecimal.TEN, LocalDateTime.now(),
                "001-1", "001-2", "TRF-000001");
    }

    @Test
    void transferenciaCorrectaRegistraConElSaldoMinimoDeLaCuenta() throws Exception {
        Cuenta origen = cuenta(new CuentaAhorro(), 1, "001-1", "1000.00");
        Cuenta destino = cuenta(new CuentaAhorro(), 2, "001-2", "50.00");
        when(cuentas.buscarDeUsuario(1, USUARIO)).thenReturn(origen);
        when(cuentas.buscarPorNumero("001-2")).thenReturn(destino);
        when(transacciones.registrarTransferencia(anyInt(), anyInt(), any(), any())).thenReturn(vista());

        TransaccionVista r = servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("100.00"));

        assertEquals("TRF-000001", r.comprobante());
        verify(transacciones).registrarTransferencia(
                eq(1), eq(2), eq(new BigDecimal("100.00")), eq(BigDecimal.ZERO));
    }

    @Test
    void ahorroNoPuedeQuedarEnNegativo() throws Exception {
        when(cuentas.buscarDeUsuario(1, USUARIO)).thenReturn(cuenta(new CuentaAhorro(), 1, "001-1", "100.00"));

        var e = assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("100.01")));

        assertEquals("Saldo insuficiente.", e.getMessage());
        verify(transacciones, never()).registrarTransferencia(anyInt(), anyInt(), any(), any());
    }

    @Test
    void corrienteSiPuedeUsarSuSobregiro() throws Exception {
        Cuenta origen = cuenta(new CuentaCorriente(), 1, "001-1", "100.00");
        Cuenta destino = cuenta(new CuentaAhorro(), 2, "001-2", "0.00");
        when(cuentas.buscarDeUsuario(1, USUARIO)).thenReturn(origen);
        when(cuentas.buscarPorNumero("001-2")).thenReturn(destino);
        when(transacciones.registrarTransferencia(anyInt(), anyInt(), any(), any())).thenReturn(vista());

        // 100 de saldo + 500 de sobregiro = 600 disponibles
        assertDoesNotThrow(() -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("600.00")));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("600.01")));
    }

    @Test
    void unaCuentaAjenaSeTrataComoInexistente() throws Exception {
        when(cuentas.buscarDeUsuario(99, USUARIO)).thenReturn(null);

        var e = assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 99, "001-2", BigDecimal.TEN));

        assertEquals("La cuenta seleccionada no existe.", e.getMessage());
    }

    @Test
    void noSePuedeTransferirALaMismaCuenta() throws Exception {
        Cuenta origen = cuenta(new CuentaAhorro(), 1, "001-1", "500.00");
        when(cuentas.buscarDeUsuario(1, USUARIO)).thenReturn(origen);
        when(cuentas.buscarPorNumero("001-1")).thenReturn(origen);

        var e = assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-1", BigDecimal.TEN));

        assertEquals("No puedes transferir a la misma cuenta.", e.getMessage());
    }

    @Test
    void montoInvalidoSeRechazaAntesDeTocarLaBase() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-2", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("-5")));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("1.005")));
        verifyNoInteractions(cuentas);
    }

    @Test
    void respetaElLimiteDiarioConfigurable() throws Exception {
        Cuenta origen = cuenta(new CuentaAhorro(), 1, "001-1", "100000.00");
        Cuenta destino = cuenta(new CuentaAhorro(), 2, "001-2", "0.00");
        when(cuentas.buscarDeUsuario(1, USUARIO)).thenReturn(origen);
        when(cuentas.buscarPorNumero("001-2")).thenReturn(destino);
        when(transacciones.totalTransferidoHoy(1)).thenReturn(new BigDecimal("4500.00"));

        // Límite por defecto 5000: ya van 4500, otros 600 lo superan
        var e = assertThrows(IllegalArgumentException.class,
                () -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("600.00")));
        assertTrue(e.getMessage().contains("límite diario"));

        // El administrador sube el límite a 10000 desde Parámetros
        Parametro p = new Parametro();
        p.setNombre("LIMITE_TRANSFERENCIA_DIARIA");
        p.setValor("10000.00");
        when(parametroDAO.buscarPorNombre("LIMITE_TRANSFERENCIA_DIARIA")).thenReturn(p);
        when(transacciones.registrarTransferencia(anyInt(), anyInt(), any(), any())).thenReturn(vista());

        assertDoesNotThrow(() -> servicio.transferir(USUARIO, 1, "001-2", new BigDecimal("600.00")));
    }

    @Test
    void pagoDeServicioUsaElCodigoDelServicio() throws Exception {
        Cuenta c = cuenta(new CuentaAhorro(), 1, "001-1", "300.00");
        when(cuentas.buscarDeUsuario(1, USUARIO)).thenReturn(c);
        when(transacciones.registrarPago(anyInt(), any(), any(), any())).thenReturn(vista());

        servicio.pagarServicio(USUARIO, 1, "luz", new BigDecimal("80.50"));

        verify(transacciones).registrarPago(eq(1), eq(new BigDecimal("80.50")), eq(BigDecimal.ZERO), eq("LUZ"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.pagarServicio(USUARIO, 1, "gas-inexistente", BigDecimal.TEN));
    }
}
