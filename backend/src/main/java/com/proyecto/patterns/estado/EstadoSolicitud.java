package com.proyecto.patterns.estado;

/**
 * PATRÓN: State (Comportamiento).
 *
 * PROBLEMA: una solicitud de crédito pasa por PENDIENTE -> EN_EVALUACION ->
 *           APROBADO o RECHAZADO, y qué acciones se permiten depende del
 *           estado (no se puede aprobar algo que nadie evaluó, ni cambiar
 *           algo ya resuelto). Resolverlo con un texto "estado" y if/else
 *           encadenados en el servicio crea código espagueti: cada estado
 *           nuevo obliga a revisar todos los if.
 * SOLUCIÓN: cada estado es una clase que SOLO sabe qué puede hacer y a qué
 *           estado pasa. La solicitud delega en su estado actual; las
 *           acciones prohibidas lanzan TransicionInvalidaException.
 * POR QUÉ NO OTRO: Strategy intercambia un algoritmo y lo elige quien llama;
 *           aquí es el propio objeto quien cambia de comportamiento al
 *           cambiar de estado. Una enumeración con switch concentraría las
 *           reglas en un solo lugar pero seguiría obligando a modificarlo
 *           para cada estado nuevo (viola Abierto/Cerrado).
 * SIN EL PATRÓN: agregar un estado (por ejemplo DESEMBOLSADO) obligaría a
 *           tocar todos los if existentes con riesgo de romper otros flujos.
 *
 * Cada método devuelve el estado SIGUIENTE (los estados no se modifican a sí
 * mismos: son objetos sin datos).
 */
public interface EstadoSolicitud {

    /** Nombre guardado en la base de datos (PENDIENTE, EN_EVALUACION...). */
    String nombre();

    EstadoSolicitud evaluar();

    EstadoSolicitud aprobar();

    EstadoSolicitud rechazar();
}
