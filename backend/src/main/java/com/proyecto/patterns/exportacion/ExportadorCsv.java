package com.proyecto.patterns.exportacion;

import com.proyecto.model.TransaccionVista;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exporta a CSV. El separador es configurable, así la variante para Excel
 * (punto y coma) reutiliza todo el código en vez de copiarlo.
 */
public class ExportadorCsv implements ExportadorTransacciones {

    private final String codigo;
    private final char separador;
    private final boolean conBom;

    /** conBom: marca UTF-8 al inicio para que Excel muestre bien las tildes. */
    public ExportadorCsv(String codigo, char separador, boolean conBom) {
        this.codigo = codigo;
        this.separador = separador;
        this.conBom = conBom;
    }

    @Override
    public String formato() { return codigo; }

    @Override
    public String tipoContenido() { return "text/csv; charset=UTF-8"; }

    @Override
    public String extension() { return "csv"; }

    @Override
    public byte[] exportar(List<TransaccionVista> transacciones) {
        StringBuilder sb = new StringBuilder();
        if (conBom) {
            sb.append('﻿');
        }
        fila(sb, "id", "tipo", "monto", "fecha", "cuenta_origen", "cuenta_destino", "comprobante");
        for (TransaccionVista t : transacciones) {
            fila(sb, String.valueOf(t.id()), t.tipo(), t.monto().toPlainString(),
                    String.valueOf(t.fecha()), t.cuentaOrigen(), t.cuentaDestino(), t.comprobante());
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void fila(StringBuilder sb, String... campos) {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(separador);
            }
            sb.append(escapar(campos[i]));
        }
        sb.append("\r\n");
    }

    /**
     * Seguridad: un texto que empieza con = + - @ lo interpretaría Excel como
     * una fórmula ("inyección de CSV"); se antepone una comilla simple. Los
     * campos con separador, comillas o saltos de línea se encierran entre comillas.
     */
    String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        String v = valor;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        boolean necesitaComillas = v.indexOf(separador) >= 0 || v.indexOf('"') >= 0
                || v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0;
        if (necesitaComillas) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
