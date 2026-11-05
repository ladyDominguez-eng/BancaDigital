package com.proyecto.service;

import com.proyecto.dao.IInformesDAO;
import com.proyecto.dao.factory.DAOFactory;
import com.proyecto.model.InformesUsuarios;
import com.proyecto.model.InformesCuentas;
import com.proyecto.model.InformesTransacciones;

import java.sql.SQLException;
import java.util.List;

public class InformesService {

    // D de SOLID: depende de la interfaz; la Factory decide la clase concreta.
    private final IInformesDAO dao = DAOFactory.crearInformesDAO();

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
