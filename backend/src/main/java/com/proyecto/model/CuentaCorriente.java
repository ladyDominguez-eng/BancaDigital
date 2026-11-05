package com.proyecto.model;

import java.math.BigDecimal;

public class CuentaCorriente extends Cuenta  {
    private static final BigDecimal SOBREGIRO = new BigDecimal("500.00");

    @Override
    public BigDecimal getSaldoDisponible() { return saldo.add(SOBREGIRO); }

    @Override
    public String getTipo() { return "CORRIENTE"; }

    // Puede quedar en negativo hasta el monto del sobregiro
    @Override
    public BigDecimal getSaldoMinimo() { return SOBREGIRO.negate(); }
}