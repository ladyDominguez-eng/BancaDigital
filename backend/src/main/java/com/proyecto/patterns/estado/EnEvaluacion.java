package com.proyecto.patterns.estado;

/** Un asesor la está revisando: puede aprobarla o rechazarla. */
public class EnEvaluacion extends EstadoBase {

    @Override
    public String nombre() { return "EN_EVALUACION"; }

    @Override
    public EstadoSolicitud aprobar() { return new Aprobado(); }

    @Override
    public EstadoSolicitud rechazar() { return new Rechazado(); }
}
