package com.proyecto.patterns.estado;

/** Estado final: ya no admite ninguna acción (todas heredan "prohibido"). */
public class Rechazado extends EstadoBase {

    @Override
    public String nombre() { return "RECHAZADO"; }
}
