package com.proyecto.dao;

import java.sql.SQLException;

/**
 * Contrato del alta de un cliente completo (usuario + cliente + cuenta).
 *
 * PRINCIPIO: S de SOLID. UsuarioDAO solo conoce la tabla usuario y ClienteDAO
 * solo la tabla cliente; el alta toca las tres a la vez y debe ser ATÓMICA
 * (RNF-03), por eso vive en su propio DAO en vez de repartirse entre los
 * otros. D de SOLID: el servicio depende de esta interfaz, no de JDBC.
 */
public interface IRegistroClienteDAO {

    /** Resultado del alta: los identificadores creados y el número de cuenta generado. */
    record Alta(int usuarioId, int clienteId, int cuentaId, String numeroCuenta) { }

    /**
     * Crea en UNA transacción el usuario (rol CLIENTE), el cliente y su
     * cuenta con saldo cero. Si cualquier paso falla no queda nada guardado.
     */
    Alta registrar(String username, String passwordHash, String dni,
            String nombres, String apellidos, String tipoCuenta) throws SQLException;

    boolean existeDni(String dni) throws SQLException;
}
