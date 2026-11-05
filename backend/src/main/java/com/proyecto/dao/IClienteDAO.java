package com.proyecto.dao;

import com.proyecto.model.Cliente;
import java.sql.SQLException;

/** Contrato de acceso a datos de Cliente (D de SOLID). */
public interface IClienteDAO {

    /** Cliente asociado a un usuario con rol CLIENTE, o null si no tiene. */
    Cliente buscarPorUsuarioId(int usuarioId) throws SQLException;
}
