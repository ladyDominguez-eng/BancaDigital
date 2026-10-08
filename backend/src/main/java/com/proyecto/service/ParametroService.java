package com.proyecto.service;

import com.proyecto.dao.ParametroDAO;
import com.proyecto.model.Parametro;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ParametroService {

    private final ParametroDAO dao = new ParametroDAO();

    public List<Parametro> listar() throws SQLException {
        return dao.listar();
    }

    public void actualizar(Long id, String valor) throws SQLException {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El valor es obligatorio.");
        }

        try {
            if (new BigDecimal(valor.trim()).signum() <= 0) {
                throw new IllegalArgumentException(
                    "El valor debe ser mayor que cero."
                );
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "El valor debe ser un número."
            );
        }

        if (!dao.actualizarValor(id, valor.trim())) {
            throw new IllegalArgumentException(
                "Parámetro no encontrado."
            );
        }
    }
}