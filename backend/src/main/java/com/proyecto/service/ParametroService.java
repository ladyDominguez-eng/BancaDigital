package com.proyecto.service;

import com.proyecto.dao.IParametroDAO;
import com.proyecto.dao.factory.DAOFactory;
import com.proyecto.model.Parametro;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ParametroService {

    // D de SOLID: depende de la interfaz; la Factory decide la clase concreta.
    private final IParametroDAO dao = DAOFactory.crearParametroDAO();

    public List<Parametro> listar() throws SQLException {
        return dao.listar();
    }

    /**
     * Lee un parámetro numérico configurado por el administrador (RF-03).
     * Si el parámetro no existe, devuelve el valor por defecto recibido.
     * Así las reglas de negocio dejan de estar fijas en el código.
     */
    public BigDecimal obtenerNumero(String nombre, BigDecimal porDefecto)
            throws SQLException {

        Parametro p = dao.buscarPorNombre(nombre);

        if (p == null) {
            return porDefecto;
        }

        try {
            return new BigDecimal(p.getValor().trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                "El parámetro " + nombre + " no tiene un valor numérico válido.");
        }
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