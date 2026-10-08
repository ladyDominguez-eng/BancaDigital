package com.proyecto.model;

import java.math.BigDecimal;

public class DepositoRequest {
    private String numeroCuenta;
    private BigDecimal monto;

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
