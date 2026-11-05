package com.proyecto.patterns.exportacion;

import com.proyecto.model.TransaccionVista;
import java.util.List;

/**
 * PATRÓN: Strategy (Comportamiento) — formatos de exportación (RF-15).
 *
 * PROBLEMA: el auditor exporta las transacciones para revisarlas fuera del
 *           sistema, y cada herramienta pide otro formato (CSV estándar,
 *           CSV para Excel en español, etc.). Un método con if por formato
 *           crece con cada formato nuevo.
 * SOLUCIÓN: cada formato es un objeto que implementa esta interfaz (una
 *           clase propia, o la misma clase configurada de otra manera, como
 *           ExportadorCsv con coma o con punto y coma); el servicio elige la
 *           estrategia según el parámetro "formato" y no conoce los
 *           detalles de ninguna.
 * POR QUÉ NO OTRO: Factory decide QUÉ objeto crear, no CÓMO se hace un
 *           trabajo; aquí lo que varía es el algoritmo de escritura.
 *           Decorator envolvería un exportador con más funciones, pero no
 *           reemplaza el formato en sí.
 * SIN EL PATRÓN: un solo método con if/else por formato; para agregar
 *           uno habría que modificar código que ya funciona (viola O).
 */
public interface ExportadorTransacciones {

    /** Código del formato que escribe el cliente (por ejemplo "csv"). */
    String formato();

    String tipoContenido();

    String extension();

    byte[] exportar(List<TransaccionVista> transacciones);
}
