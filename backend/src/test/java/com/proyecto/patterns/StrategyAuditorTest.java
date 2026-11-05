package com.proyecto.patterns;

import static org.junit.jupiter.api.Assertions.*;

import com.proyecto.model.TransaccionVista;
import com.proyecto.patterns.exportacion.ExportadorTransacciones;
import com.proyecto.patterns.exportacion.Exportadores;
import com.proyecto.patterns.riesgo.AlertaRiesgo;
import com.proyecto.patterns.riesgo.MontoAltoRegla;
import com.proyecto.patterns.riesgo.OperacionesFrecuentesRegla;
import com.proyecto.patterns.riesgo.ReglaRiesgo;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas de las estrategias de riesgo y de exportación (sin base de datos). */
class StrategyAuditorTest {

    private TransaccionVista tx(int id, String tipo, String monto, String origen, String destino, LocalDateTime f) {
        return new TransaccionVista(id, tipo, new BigDecimal(monto), f, origen, destino, "C-" + id);
    }

    private final LocalDateTime hoy = LocalDateTime.of(2026, 11, 4, 10, 0);

    // ---------------- Reglas de riesgo ----------------

    @Test
    void montoAltoMarcaLosMontosQueLlegaAlUmbral() {
        ReglaRiesgo regla = new MontoAltoRegla(new BigDecimal("3000"));
        List<TransaccionVista> lista = List.of(
                tx(1, "TRANSFERENCIA", "2999.99", "A", "B", hoy),
                tx(2, "TRANSFERENCIA", "3000.00", "A", "B", hoy),
                tx(3, "DEPOSITO", "9000.00", null, "B", hoy));

        List<AlertaRiesgo> alertas = regla.evaluar(lista);

        assertEquals(2, alertas.size());
        assertEquals(2, alertas.get(0).transaccionId());
        assertEquals("B", alertas.get(1).cuenta()); // depósito: la cuenta es la de destino
    }

    @Test
    void operacionesFrecuentesAgrupaPorCuentaYDia() {
        ReglaRiesgo regla = new OperacionesFrecuentesRegla(2);
        List<TransaccionVista> lista = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            lista.add(tx(i, "RETIRO", "10", "A", null, hoy.minusMinutes(i)));   // 3 salidas de A hoy
        }
        lista.add(tx(4, "RETIRO", "10", "B", null, hoy));                        // 1 salida de B
        lista.add(tx(5, "RETIRO", "10", "A", null, hoy.minusDays(1)));           // otro día: no suma
        lista.add(tx(6, "DEPOSITO", "10", null, "A", hoy));                      // entrada: no cuenta

        List<AlertaRiesgo> alertas = regla.evaluar(lista);

        assertEquals(1, alertas.size());
        assertEquals("A", alertas.get(0).cuenta());
        assertTrue(alertas.get(0).descripcion().startsWith("3 operaciones"));
    }

    @Test
    void lasReglasSonIntercambiablesYSeCombinan() {
        // Strategy: el reporte solo conoce la interfaz, no las clases concretas
        List<ReglaRiesgo> reglas = List.of(new MontoAltoRegla(new BigDecimal("100")), new OperacionesFrecuentesRegla(0));
        List<TransaccionVista> lista = List.of(tx(1, "RETIRO", "500", "A", null, hoy));

        int total = reglas.stream().mapToInt(r -> r.evaluar(lista).size()).sum();

        assertEquals(2, total);
    }

    // ---------------- Exportación ----------------

    private String texto(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    void csvEstandarUsaComaYTrae_EncabezadoYFilas() {
        ExportadorTransacciones e = Exportadores.para("csv");
        String csv = texto(e.exportar(List.of(tx(1, "TRANSFERENCIA", "100.50", "001-1", "001-2", hoy))));

        String[] lineas = csv.split("\r\n");
        assertEquals("id,tipo,monto,fecha,cuenta_origen,cuenta_destino,comprobante", lineas[0]);
        assertEquals("1,TRANSFERENCIA,100.50,2026-11-04T10:00,001-1,001-2,C-1", lineas[1]);
    }

    @Test
    void csvParaExcelUsaPuntoYComaYMarcaUtf8() {
        byte[] bytes = Exportadores.para("excel").exportar(List.of(tx(1, "RETIRO", "10", "A", null, hoy)));

        assertEquals('﻿', texto(bytes).charAt(0));
        assertTrue(texto(bytes).contains("id;tipo;monto"));
    }

    @Test
    void unTextoQueEmpiezaConFormulaSeNeutraliza() {
        // "inyección de CSV": Excel ejecutaría =cmd... como fórmula
        TransaccionVista maliciosa = tx(1, "RETIRO", "10", "=1+1", null, hoy);

        String csv = texto(Exportadores.para("csv").exportar(List.of(maliciosa)));

        assertTrue(csv.contains("'=1+1"));
        assertFalse(csv.contains(",=1+1"));
    }

    @Test
    void camposConSeparadorSeEncierranEntreComillas() {
        TransaccionVista t = tx(1, "RETIRO", "10", "A,B", null, hoy);

        String csv = texto(Exportadores.para("csv").exportar(List.of(t)));

        assertTrue(csv.contains("\"A,B\""));
    }

    @Test
    void unFormatoDesconocidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> Exportadores.para("pdf"));
        assertThrows(IllegalArgumentException.class, () -> Exportadores.para(null));
    }
}
