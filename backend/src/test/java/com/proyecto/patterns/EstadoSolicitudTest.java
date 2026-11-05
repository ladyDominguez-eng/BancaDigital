package com.proyecto.patterns;

import static org.junit.jupiter.api.Assertions.*;

import com.proyecto.model.SolicitudCredito;
import com.proyecto.util.TransicionInvalidaException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Pruebas del patrón State en la solicitud de crédito (sin base de datos). */
class EstadoSolicitudTest {

    private SolicitudCredito nueva() {
        SolicitudCredito s = new SolicitudCredito();
        s.setMonto(new BigDecimal("12000.00"));
        s.setPlazoMeses(12);
        return s;
    }

    @Test
    void empiezaPendiente() {
        assertEquals("PENDIENTE", nueva().getEstado());
    }

    @Test
    void flujoCompletoHastaAprobada() {
        SolicitudCredito s = nueva();
        s.evaluar();
        assertEquals("EN_EVALUACION", s.getEstado());
        s.aprobar();
        assertEquals("APROBADO", s.getEstado());
    }

    @Test
    void flujoCompletoHastaRechazada() {
        SolicitudCredito s = nueva();
        s.evaluar();
        s.rechazar();
        assertEquals("RECHAZADO", s.getEstado());
    }

    @Test
    void noSePuedeAprobarNiRechazarSinEvaluar() {
        assertThrows(TransicionInvalidaException.class, () -> nueva().aprobar());
        assertThrows(TransicionInvalidaException.class, () -> nueva().rechazar());
    }

    @Test
    void noSePuedeEvaluarDosVeces() {
        SolicitudCredito s = nueva();
        s.evaluar();
        assertThrows(TransicionInvalidaException.class, s::evaluar);
    }

    @Test
    void losEstadosFinalesNoAdmitenCambios() {
        SolicitudCredito aprobada = nueva();
        aprobada.evaluar();
        aprobada.aprobar();
        assertThrows(TransicionInvalidaException.class, aprobada::rechazar);
        assertThrows(TransicionInvalidaException.class, aprobada::evaluar);

        SolicitudCredito rechazada = nueva();
        rechazada.evaluar();
        rechazada.rechazar();
        assertThrows(TransicionInvalidaException.class, rechazada::aprobar);
    }

    @Test
    void elEstadoSeRecuperaDesdeSuNombreGuardado() {
        SolicitudCredito s = nueva();
        s.setEstado("EN_EVALUACION");
        s.aprobar();
        assertEquals("APROBADO", s.getEstado());
        assertThrows(IllegalArgumentException.class, () -> s.setEstado("INVENTADO"));
    }

    @Test
    void cuotaMensualDelSistemaFrances() {
        // 12 000 a 12 meses con 18.5 % anual (valor verificado con una cuenta independiente)
        assertEquals(new BigDecimal("1103.02"), nueva().calcularCuota(new BigDecimal("18.50")));

        // Sin interés: 12 000 / 12
        assertEquals(new BigDecimal("1000.00"), nueva().calcularCuota(BigDecimal.ZERO));
    }
}
