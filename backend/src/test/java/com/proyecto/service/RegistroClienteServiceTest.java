package com.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.proyecto.dao.IParametroDAO;
import com.proyecto.dao.IRegistroClienteDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.model.ClienteRegistrado;
import com.proyecto.model.RegistrarClienteRequest;
import com.proyecto.model.TransaccionVista;
import com.proyecto.model.Usuario;
import com.proyecto.patterns.AuditoriaPublisher;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del alta de clientes con DAO simulados (sin base de datos). */
class RegistroClienteServiceTest {

    private IRegistroClienteDAO registro;
    private IUsuarioDAO usuarios;
    private ITransaccionDAO transacciones;
    private AuditoriaPublisher auditoria;
    private RegistroClienteService servicio;

    @BeforeEach
    void preparar() throws Exception {
        registro = mock(IRegistroClienteDAO.class);
        usuarios = mock(IUsuarioDAO.class);
        transacciones = mock(ITransaccionDAO.class);
        auditoria = mock(AuditoriaPublisher.class);
        IParametroDAO parametroDAO = mock(IParametroDAO.class);
        when(parametroDAO.buscarPorNombre(any())).thenReturn(null); // límite por defecto
        when(registro.registrar(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new IRegistroClienteDAO.Alta(10, 20, 30, "001-0000015"));
        // el codificador simulado evita el costo de BCrypt en la prueba
        servicio = new RegistroClienteService(registro, usuarios, transacciones,
                new ParametroService(parametroDAO), auditoria, clave -> "hash:" + clave);
    }

    private RegistrarClienteRequest datos(String saldo) {
        return new RegistrarClienteRequest("ana.perez", "Clave1234", "12345678",
                "Ana María", "Pérez Soto", "ahorro", saldo == null ? null : new BigDecimal(saldo));
    }

    @Test
    void registraClienteConDepositoInicialYAudita() throws Exception {
        when(transacciones.registrarDeposito(eq(30), any())).thenReturn(
                new TransaccionVista(7, "DEPOSITO", BigDecimal.TEN, LocalDateTime.now(), null, "001-0000015", "DEP-000007"));

        ClienteRegistrado r = servicio.registrar(1, datos("100.00"));

        assertEquals("001-0000015", r.numeroCuenta());
        assertEquals("DEP-000007", r.comprobante());
        // la contraseña nunca llega en claro al DAO y el tipo se normaliza
        verify(registro).registrar("ana.perez", "hash:Clave1234", "12345678", "Ana María", "Pérez Soto", "AHORRO");
        verify(auditoria).publicar(any());
    }

    @Test
    void sinSaldoInicialNoHayDeposito() throws Exception {
        ClienteRegistrado r = servicio.registrar(1, datos(null));

        assertNull(r.comprobante());
        verify(transacciones, never()).registrarDeposito(anyInt(), any());
    }

    @Test
    void datosInvalidosSeRechazanSinTocarLaBase() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1,
                new RegistrarClienteRequest("ana.perez", "Clave1234", "123", "Ana", "Pérez", "AHORRO", null)));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1,
                new RegistrarClienteRequest("ana.perez", "Clave1234", "12345678", "Ana1", "Pérez", "AHORRO", null)));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1,
                new RegistrarClienteRequest("ana.perez", "corta", "12345678", "Ana", "Pérez", "AHORRO", null)));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1,
                new RegistrarClienteRequest("ana.perez", "Clave1234", "12345678", "Ana", "Pérez", "PLATINO", null)));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, datos("-5")));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, datos("50000.01")));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, null));
        verify(registro, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void usuarioOdniRepetidoSeRechazan() throws Exception {
        when(usuarios.buscarPorUsername("ana.perez")).thenReturn(new Usuario());
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, datos(null)));

        when(usuarios.buscarPorUsername("ana.perez")).thenReturn(null);
        when(registro.existeDni("12345678")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, datos(null)));

        verify(registro, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void siFallaElDepositoInicialElMensajeIndicaLaCuentaCreada() throws Exception {
        when(transacciones.registrarDeposito(anyInt(), any())).thenThrow(new SQLException("caída"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(1, datos("100.00")));

        assertTrue(e.getMessage().contains("001-0000015"), "debe decir qué cuenta quedó creada");
    }
}
