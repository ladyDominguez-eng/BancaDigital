package com.proyecto.patterns.riesgo;

import com.proyecto.model.TransaccionVista;
import java.util.List;

/**
 * PATRÓN: Strategy (Comportamiento) — reglas del reporte de riesgo (RF-12).
 *
 * PROBLEMA: el auditor necesita detectar operaciones sospechosas con varios
 *           criterios (monto alto, demasiadas operaciones en un día, y los
 *           que se agreguen después). Si todos los criterios estuvieran
 *           dentro de un único método con if encadenados, cada criterio
 *           nuevo obligaría a modificarlo y a volver a probarlo entero.
 * SOLUCIÓN: cada criterio es una clase que implementa esta interfaz. El
 *           reporte recibe una lista de reglas y las aplica todas sin saber
 *           cuáles son.
 * POR QUÉ NO OTRO: State cambia el comportamiento de un objeto según su
 *           propio estado; aquí las reglas no cambian a nadie de estado, son
 *           algoritmos intercambiables que se eligen y combinan desde fuera.
 *           Template Method obligaría a que todas las reglas siguieran los
 *           mismos pasos; Strategy permite que cada una trabaje distinto.
 * SIN EL PATRÓN: un método gigante con todos los criterios (objeto
 *           todopoderoso) que se rompe cada vez que cambia una regla.
 */
public interface ReglaRiesgo {

    /** Nombre corto de la regla, que se muestra en el reporte. */
    String nombre();

    /** Devuelve las alertas que esta regla detecta en las transacciones dadas. */
    List<AlertaRiesgo> evaluar(List<TransaccionVista> transacciones);
}
