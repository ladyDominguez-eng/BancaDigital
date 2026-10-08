package com.proyecto.dao;

import com.proyecto.model.Bitacora;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IBitacoraDAO {

    List<Bitacora> listar(
            Integer usuarioId,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) throws SQLException;

    void registrar(
            int usuarioId,
            String accion
    ) throws SQLException;
}