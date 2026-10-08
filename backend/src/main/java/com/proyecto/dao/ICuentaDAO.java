package com.proyecto.dao;

import com.proyecto.model.Cuenta;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface ICuentaDAO {
    Cuenta buscarPorNumero(String numero) throws SQLException;
    List<Cuenta> buscarPorDni(String dni) throws SQLException;
    int depositar(String numero, BigDecimal monto) throws SQLException;
}