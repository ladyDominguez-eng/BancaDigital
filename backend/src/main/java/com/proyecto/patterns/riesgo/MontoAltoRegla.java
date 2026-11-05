package com.proyecto.patterns.riesgo;

import com.proyecto.model.TransaccionVista;
import java.math.BigDecimal;
import java.util.List;

/** Marca toda operación cuyo monto alcanza o supera un umbral. */
public class MontoAltoRegla implements ReglaRiesgo {

    private final BigDecimal umbral;

    public MontoAltoRegla(BigDecimal umbral) {
        this.umbral = umbral;
    }

    @Override
    public String nombre() {
        return "MONTO_ALTO";
    }

    @Override
    public List<AlertaRiesgo> evaluar(List<TransaccionVista> transacciones) {
        return transacciones.stream()
                .filter(t -> t.monto().compareTo(umbral) >= 0)
                .map(t -> new AlertaRiesgo(nombre(),
                        t.tipo() + " de S/ " + t.monto() + " (umbral S/ " + umbral + ")",
                        t.id(), cuenta(t), t.monto(), t.fecha()))
                .toList();
    }

    static String cuenta(TransaccionVista t) {
        return t.cuentaOrigen() != null ? t.cuentaOrigen() : t.cuentaDestino();
    }
}
