package com.proyecto.dao;

import com.proyecto.model.SolicitudCredito;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Contrato de acceso a datos de las solicitudes de crédito (D de SOLID). */
public interface ISolicitudCreditoDAO {

    /** Crea una solicitud PENDIENTE y devuelve su id. */
    int crear(int clienteId, BigDecimal monto, int plazoMeses) throws SQLException;

    SolicitudCredito buscarPorId(int id) throws SQLException;

    List<SolicitudCredito> listarPorCliente(int clienteId) throws SQLException;

    /** Lista todas o solo las de un estado (estado null = todas). */
    List<SolicitudCredito> listar(String estado) throws SQLException;

    /**
     * Cambia el estado SOLO si todavía está en estadoAnterior. Devuelve false
     * si otro asesor la cambió mientras tanto (evita pisar su decisión).
     * observaciones null conserva las que ya tenía.
     */
    boolean actualizarEstado(int id, String estadoAnterior, String estadoNuevo,
            Integer asesorId, String observaciones) throws SQLException;

    boolean actualizarObservaciones(int id, int asesorId, String observaciones) throws SQLException;
}
