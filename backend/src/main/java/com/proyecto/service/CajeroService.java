package com.proyecto.service;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.factory.DAOFactory;
import com.proyecto.model.Cuenta ;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CajeroService {

    private static final BigDecimal MONTO_MAXIMO_DEPOSITO = new BigDecimal("50000.00");

    private final ICuentaDAO dao = DAOFactory.crearCuentaDAO();

    public List<Cuenta> consultar(String dni, String numero) throws SQLException {
        if (numero != null && !numero.isBlank()) {
            Cuenta c = dao.buscarPorNumero(numero.trim());
            List<Cuenta> lista = new ArrayList<>();
            if (c != null) lista.add(c);
            return lista;
        }
        if (dni != null && !dni.isBlank()) {
            return dao.buscarPorDni(dni.trim());
        }
        throw new IllegalArgumentException("Ingresa un DNI o un número de cuenta.");
    }

    public int depositar(String numero, BigDecimal monto) throws SQLException {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de cuenta es obligatorio.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        if (monto.compareTo(MONTO_MAXIMO_DEPOSITO) > 0) {
            throw new IllegalArgumentException("El monto supera el máximo permitido.");
        }
        Cuenta c = dao.buscarPorNumero(numero.trim());
        if (c == null) throw new IllegalArgumentException("La cuenta no existe.");
        if (!"ACTIVA".equals(c.getEstado())) throw new IllegalArgumentException("La cuenta no está activa.");
        return dao.depositar(numero.trim(), monto);
    }
}
