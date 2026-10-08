package com.proyecto.model;

import java.math.BigDecimal;

public class InformesCuentas {

    private int totalClientes;
    private int totalCuentas;
    private BigDecimal saldoTotal;

    public InformesCuentas() {
    }

    public int getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public int getTotalCuentas() {
        return totalCuentas;
    }

    public void setTotalCuentas(int totalCuentas) {
        this.totalCuentas = totalCuentas;
    }

    public BigDecimal getSaldoTotal() {
        return saldoTotal;
    }

    public void setSaldoTotal(BigDecimal saldoTotal) {
        this.saldoTotal = saldoTotal;
    }
}