package com.proyecto.model;

import java.math.BigDecimal;

public class CuentaAhorro extends Cuenta  {
    @Override
    public BigDecimal getSaldoDisponible() { return saldo; }

    @Override
    public String getTipo() { return "AHORRO"; }
}