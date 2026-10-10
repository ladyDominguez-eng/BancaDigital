package com.proyecto.service;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.factory.DAOFactory;
import com.proyecto.model.Cuenta;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CajeroService {

    // LÍMITE: depósito máximo permitido
    private static final BigDecimal MONTO_MAXIMO_DEPOSITO =
            new BigDecimal("50000.00");

    // FACTORY: obtiene el DAO de cuentas mediante su interfaz
    private final ICuentaDAO dao = DAOFactory.crearCuentaDAO();

    // CONSULTAR: busca por número de cuenta o por DNI
    public List<Cuenta> consultar(String dni, String numero)
            throws SQLException {

        // VALIDACIÓN: comprueba si ingresaron número de cuenta
        if (numero != null && !numero.isBlank()) {

            // BÚSQUEDA: consulta la cuenta por número
            Cuenta c = dao.buscarPorNumero(numero.trim());

            // LISTA: prepara el resultado
            List<Cuenta> lista = new ArrayList<>();

            // RESULTADO: agrega la cuenta si existe
            if (c != null) lista.add(c);

            return lista;
        }

        // BÚSQUEDA: consulta las cuentas por DNI
        if (dni != null && !dni.isBlank()) {
            return dao.buscarPorDni(dni.trim());
        }

        // ERROR: no ingresó DNI ni número de cuenta
        throw new IllegalArgumentException(
                "Ingresa un DNI o un número de cuenta.");
    }

    // DEPOSITAR: valida y realiza el depósito
    public int depositar(String numero, BigDecimal monto)
            throws SQLException {

        // VALIDACIÓN: número obligatorio
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException(
                    "El número de cuenta es obligatorio.");
        }

        // VALIDACIÓN: monto mayor que cero
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero.");
        }

        // VALIDACIÓN: respeta el límite máximo
        if (monto.compareTo(MONTO_MAXIMO_DEPOSITO) > 0) {
            throw new IllegalArgumentException(
                    "El monto supera el máximo permitido.");
        }

        // BÚSQUEDA: comprueba que la cuenta exista
        Cuenta c = dao.buscarPorNumero(numero.trim());

        // ERROR: cuenta inexistente
        if (c == null)
            throw new IllegalArgumentException("La cuenta no existe.");

        // VALIDACIÓN: cuenta activa
        if (!"ACTIVA".equals(c.getEstado()))
            throw new IllegalArgumentException(
                    "La cuenta no está activa.");

        // OPERACIÓN: solicita al DAO realizar el depósito
        return dao.depositar(numero.trim(), monto);
    }
}
