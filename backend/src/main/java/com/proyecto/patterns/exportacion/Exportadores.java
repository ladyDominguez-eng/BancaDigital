package com.proyecto.patterns.exportacion;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registro de las estrategias de exportación disponibles. Para ofrecer un
 * formato nuevo basta crear su clase y agregarla a esta lista: ni el
 * servicio ni el controlador cambian (O de SOLID).
 */
public final class Exportadores {

    private static final Map<String, ExportadorTransacciones> POR_FORMATO =
            List.<ExportadorTransacciones>of(
                    new ExportadorCsv("csv", ',', false),
                    new ExportadorCsv("excel", ';', true))
            .stream()
            .collect(Collectors.toMap(ExportadorTransacciones::formato, e -> e));

    private Exportadores() { }

    public static ExportadorTransacciones para(String formato) {
        ExportadorTransacciones e = formato == null ? null : POR_FORMATO.get(formato.trim().toLowerCase());
        if (e == null) {
            throw new IllegalArgumentException(
                "Formato de exportación no válido. Usa: " + String.join(", ", POR_FORMATO.keySet()) + ".");
        }
        return e;
    }
}
