package com.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

public abstract class Cuenta  {
    protected int id;
    protected String numero;
    protected String titular;
    protected String dni;
    protected BigDecimal saldo;
    protected String estado;

    // Cada tipo de cuenta calcula su saldo disponible distinto (polimorfismo)
    public abstract BigDecimal getSaldoDisponible();
    public abstract String getTipo();

    /**
     * GRASP Experto: la cuenta sabe hasta dónde puede bajar su saldo.
     * Ahorro: 0 (sin sobregiro). Corriente: negativo (sobregiro permitido).
     * Los retiros y transferencias usan este valor, así no hay un "if por
     * tipo de cuenta" repartido por el código (principio L y O de SOLID).
     */
    @JsonIgnore
    public BigDecimal getSaldoMinimo() { return BigDecimal.ZERO; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}