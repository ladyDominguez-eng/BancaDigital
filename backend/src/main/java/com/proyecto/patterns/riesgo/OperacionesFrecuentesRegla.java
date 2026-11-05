package com.proyecto.patterns.riesgo;

import com.proyecto.model.TransaccionVista;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Marca las cuentas que hacen salir dinero (retiro, transferencia o pago)
 * demasiadas veces en un mismo día. Genera UNA alerta por cuenta y día.
 */
public class OperacionesFrecuentesRegla implements ReglaRiesgo {

    private final int maximoPorDia;

    public OperacionesFrecuentesRegla(int maximoPorDia) {
        this.maximoPorDia = maximoPorDia;
    }

    @Override
    public String nombre() {
        return "OPERACIONES_FRECUENTES";
    }

    @Override
    public List<AlertaRiesgo> evaluar(List<TransaccionVista> transacciones) {

        // Se agrupan las salidas de dinero por (cuenta, día)
        Map<String, List<TransaccionVista>> grupos = new LinkedHashMap<>();
        for (TransaccionVista t : transacciones) {
            if (t.cuentaOrigen() == null || t.fecha() == null) {
                continue; // un depósito no es una salida de dinero
            }
            LocalDate dia = t.fecha().toLocalDate();
            grupos.computeIfAbsent(t.cuentaOrigen() + "|" + dia, k -> new ArrayList<>()).add(t);
        }

        List<AlertaRiesgo> alertas = new ArrayList<>();
        for (List<TransaccionVista> grupo : grupos.values()) {
            if (grupo.size() > maximoPorDia) {
                TransaccionVista ultima = grupo.get(0); // la lista viene de más reciente a más antigua
                alertas.add(new AlertaRiesgo(nombre(),
                        grupo.size() + " operaciones de salida el " + ultima.fecha().toLocalDate()
                                + " (máximo " + maximoPorDia + ")",
                        ultima.id(), ultima.cuentaOrigen(), ultima.monto(), ultima.fecha()));
            }
        }
        return alertas;
    }
}
