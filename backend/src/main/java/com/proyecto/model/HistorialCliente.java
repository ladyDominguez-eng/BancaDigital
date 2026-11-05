package com.proyecto.model;

import java.util.List;

/** Historial financiero de un cliente para el asesor (RF: consultar historial). */
public record HistorialCliente(List<SolicitudCredito> solicitudes, List<TransaccionVista> movimientos) { }
