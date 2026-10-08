package com.proyecto.patterns;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaPublisher {

    private static final AuditoriaPublisher instancia =
            new AuditoriaPublisher();

    private final List<ObservadorAuditoria> observadores =
            new ArrayList<>();

    private AuditoriaPublisher() {
    agregarObservador(
        new AuditoriaObserver(
            new com.proyecto.dao.BitacoraDAO()
        )
    );
}

    public static AuditoriaPublisher getInstancia() {
        return instancia;
    }

    public void agregarObservador(
            ObservadorAuditoria observador
    ) {
        observadores.add(observador);
    }

    public void publicar(EventoAuditoria evento) {

        for (ObservadorAuditoria observador : observadores) {
            observador.actualizar(evento);
        }
    }
}