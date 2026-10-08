package com.proyecto.model;

import java.math.BigDecimal;

public class ResultadoOperacionCajero {

    private boolean exitoso;
    private String mensaje;
    private BigDecimal saldoActual;

    public ResultadoOperacionCajero() {
    }

    public ResultadoOperacionCajero(
            boolean exitoso,
            String mensaje,
            BigDecimal saldoActual) {

        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.saldoActual = saldoActual;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }
}
