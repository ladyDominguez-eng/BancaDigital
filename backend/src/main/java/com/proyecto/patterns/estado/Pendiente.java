package com.proyecto.patterns.estado;

/** Recién solicitada: lo único permitido es empezar a evaluarla. */
public class Pendiente extends EstadoBase {

    @Override
    public String nombre() { return "PENDIENTE"; }

    @Override
    public EstadoSolicitud evaluar() { return new EnEvaluacion(); }
}
