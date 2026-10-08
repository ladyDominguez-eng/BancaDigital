package com.proyecto.service;

import com.proyecto.dao.InformesDAO;
import com.proyecto.model.InformesUsuarios;
import com.proyecto.model.InformesCuentas;
import com.proyecto.model.InformesTransacciones;

import java.sql.SQLException;
import java.util.List;

public class InformesService {

    private final InformesDAO dao = new InformesDAO();

    public List<InformesUsuarios> obtenerUsuariosPorRol()
            throws SQLException {

        return dao.usuariosPorRol();
    }

    public InformesCuentas obtenerResumenCuentas()
            throws SQLException {

        return dao.resumenCuentas();
    }

    public List<InformesTransacciones> obtenerTransaccionesPorTipo()
            throws SQLException {

        return dao.transaccionesPorTipo();
    }
}
