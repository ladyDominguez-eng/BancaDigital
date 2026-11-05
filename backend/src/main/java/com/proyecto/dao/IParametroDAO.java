package com.proyecto.dao;

import com.proyecto.model.Parametro;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de los parámetros del sistema.
 * PRINCIPIO: D de SOLID. ParametroService depende de esta interfaz y no
 * de la clase concreta con JDBC.
 */
public interface IParametroDAO {
    List<Parametro> listar() throws SQLException;
    boolean actualizarValor(Long id, String valor) throws SQLException;
}
