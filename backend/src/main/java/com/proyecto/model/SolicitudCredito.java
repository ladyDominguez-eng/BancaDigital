package com.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proyecto.patterns.estado.EstadoSolicitud;
import com.proyecto.patterns.estado.Estados;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Solicitud de crédito de un cliente.
 *
 * State: el estado es un OBJETO (EstadoSolicitud) y las acciones evaluar /
 * aprobar / rechazar se delegan en él. La solicitud no tiene ningún "if" por
 * estado.
 * GRASP Experto: la solicitud tiene el monto y el plazo, así que ella misma
 * calcula su cuota mensual.
 */
public class SolicitudCredito {

    private int id;
    private int clienteId;
    private String clienteNombre;
    private String clienteDni;
    private Integer asesorId;
    private BigDecimal monto;
    private int plazoMeses;
    private String observaciones;
    private LocalDateTime fecha;
    private EstadoSolicitud estado = Estados.desdeNombre(null);
    private BigDecimal cuotaMensual;

    // ---------------- Acciones (delegan en el estado) ----------------

    public void evaluar()  { estado = estado.evaluar(); }
    public void aprobar()  { estado = estado.aprobar(); }
    public void rechazar() { estado = estado.rechazar(); }

    /**
     * Cuota mensual del sistema francés: C = M·r / (1 − (1+r)^−n), con r la
     * tasa mensual. Si la tasa es 0 se divide el monto en partes iguales.
     */
    public BigDecimal calcularCuota(BigDecimal tasaAnualPorcentaje) {
        BigDecimal r = tasaAnualPorcentaje
                .divide(new BigDecimal("1200"), MathContext.DECIMAL64);
        if (r.signum() == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = BigDecimal.ONE.add(r).pow(plazoMeses, MathContext.DECIMAL64);
        BigDecimal denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, MathContext.DECIMAL64));
        return monto.multiply(r).divide(denominador, 2, RoundingMode.HALF_UP);
    }

    // ---------------- Estado ----------------

    // En el JSON y en la base de datos el estado es su nombre (texto)
    public String getEstado() { return estado.nombre(); }
    public void setEstado(String nombre) { this.estado = Estados.desdeNombre(nombre); }

    @JsonIgnore
    public EstadoSolicitud getEstadoActual() { return estado; }

    // ---------------- Datos ----------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }
    public Integer getAsesorId() { return asesorId; }
    public void setAsesorId(Integer asesorId) { this.asesorId = asesorId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public BigDecimal getCuotaMensual() { return cuotaMensual; }
    public void setCuotaMensual(BigDecimal cuotaMensual) { this.cuotaMensual = cuotaMensual; }
}
