package com.proyecto.dao;

import com.proyecto.model.Cuenta;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface ICuentaDAO {
    Cuenta buscarPorNumero(String numero) throws SQLException;
    List<Cuenta> buscarPorDni(String dni) throws SQLException;
    int depositar(String numero, BigDecimal monto) throws SQLException;

    /** Cuentas del cliente asociado a ese usuario (para el rol CLIENTE). */
    List<Cuenta> listarPorUsuario(int usuarioId) throws SQLException;

    /** Cuenta por id, o null si no existe. */
    Cuenta buscarPorId(int id) throws SQLException;

    /** Cuenta por id SOLO si pertenece a ese usuario; si es ajena devuelve null. */
    Cuenta buscarDeUsuario(int cuentaId, int usuarioId) throws SQLException;
}