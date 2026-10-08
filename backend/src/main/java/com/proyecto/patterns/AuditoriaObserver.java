package com.proyecto.patterns;

import com.proyecto.dao.IBitacoraDAO;

public class AuditoriaObserver implements ObservadorAuditoria {

    private final IBitacoraDAO dao;

    public AuditoriaObserver(IBitacoraDAO dao) {
        this.dao = dao;
    }

    @Override
    public void actualizar(EventoAuditoria evento) {

        try {

            dao.registrar(
                evento.getUsuarioId(),
                evento.getAccion()
            );

        } catch (Exception e) {

            System.err.println(
                "No se pudo registrar la auditoría: "
                + e.getMessage()
            );
        }
    }
}