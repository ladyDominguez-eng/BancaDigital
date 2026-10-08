package com.proyecto.model;

import java.math.BigDecimal;

public class CuentaCorriente extends Cuenta  {
    private static final BigDecimal SOBREGIRO = new BigDecimal("500.00");

    @Override
    public BigDecimal getSaldoDisponible() { return saldo.add(SOBREGIRO); }

    @Override
    public String getTipo() { return "CORRIENTE"; }
}