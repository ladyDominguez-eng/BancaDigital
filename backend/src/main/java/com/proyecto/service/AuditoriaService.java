package com.proyecto.service;

import com.proyecto.dao.IBitacoraDAO;
import com.proyecto.model.Bitacora;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AuditoriaService {

    private final IBitacoraDAO dao;

    public AuditoriaService(IBitacoraDAO dao) {
        this.dao = dao;
    }

    public List<Bitacora> listar(
            Integer usuarioId,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) throws SQLException {

        return dao.listar(
                usuarioId,
                fechaDesde,
                fechaHasta
        );
    }

    public void registrar(
            int usuarioId,
            String accion
    ) throws SQLException {

        dao.registrar(usuarioId, accion);
    }
}