package com.proyecto.patterns;

public class EventoAuditoria {

    private final int usuarioId;
    private final String accion;

    public EventoAuditoria(int usuarioId, String accion) {
        this.usuarioId = usuarioId;
        this.accion = accion;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getAccion() {
        return accion;
    }
}