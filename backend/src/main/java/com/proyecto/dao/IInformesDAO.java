package com.proyecto.dao;

import com.proyecto.model.InformesCuentas;
import com.proyecto.model.InformesTransacciones;
import com.proyecto.model.InformesUsuarios;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de las consultas de reportes.
 * PRINCIPIO: D de SOLID. InformesService depende de esta interfaz y no
 * de la clase concreta con JDBC.
 */
public interface IInformesDAO {
    List<InformesUsuarios> usuariosPorRol() throws SQLException;
    InformesCuentas resumenCuentas() throws SQLException;
    List<InformesTransacciones> transaccionesPorTipo() throws SQLException;
}
