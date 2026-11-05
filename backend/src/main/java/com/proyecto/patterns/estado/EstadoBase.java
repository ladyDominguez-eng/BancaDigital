package com.proyecto.patterns.estado;

import com.proyecto.util.TransicionInvalidaException;

/**
 * Comportamiento por defecto de todos los estados: NO permitir la acción.
 * Cada estado concreto solo redefine las acciones que sí permite. Así no se
 * repite el mismo "lanzar error" en cada clase (antipatrón copiar y pegar).
 */
abstract class EstadoBase implements EstadoSolicitud {

    @Override
    public EstadoSolicitud evaluar() {
        throw prohibida("evaluar");
    }

    @Override
    public EstadoSolicitud aprobar() {
        throw prohibida("aprobar");
    }

    @Override
    public EstadoSolicitud rechazar() {
        throw prohibida("rechazar");
    }

    private TransicionInvalidaException prohibida(String accion) {
        return new TransicionInvalidaException(
            "No se puede " + accion + " una solicitud en estado " + nombre() + ".");
    }
}
