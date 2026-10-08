package com.proyecto.service;

import com.proyecto.dao.CajeroDAO;
import com.proyecto.model.Movimiento;
import com.proyecto.model.ResultadoOperacionCajero;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CajeroService {

    private final CajeroDAO cajeroDAO;

    private static final BigDecimal MAX_DEPOSITO =
            new BigDecimal("10000.00");

    private static final BigDecimal MAX_RETIRO =
            new BigDecimal("5000.00");

    public CajeroService(CajeroDAO cajeroDAO) {
        this.cajeroDAO = cajeroDAO;
    }

    public ResultadoOperacionCajero registrarDeposito(
            Movimiento movimiento) {

        String error = validarMovimiento(movimiento);

        if (error != null) {
            return new ResultadoOperacionCajero(
                    false,
                    error,
                    null
            );
        }

        if (movimiento.getMonto().compareTo(MAX_DEPOSITO) > 0) {

            return new ResultadoOperacionCajero(
                    false,
                    "El depósito supera el monto máximo permitido de S/ 10,000.00.",
                    null
            );
        }

        try {

            return cajeroDAO.ejecutarOperacion(
                    movimiento,
                    "DEPOSITO"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new ResultadoOperacionCajero(
                    false,
                    "Ocurrió un error al registrar el depósito.",
                    null
            );
        }
    }

    public ResultadoOperacionCajero registrarRetiro(
            Movimiento movimiento) {

        String error = validarMovimiento(movimiento);

        if (error != null) {
            return new ResultadoOperacionCajero(
                    false,
                    error,
                    null
            );
        }

        if (movimiento.getMonto().compareTo(MAX_RETIRO) > 0) {

            return new ResultadoOperacionCajero(
                    false,
                    "El retiro supera el monto máximo permitido de S/ 5,000.00.",
                    null
            );
        }

        try {

            return cajeroDAO.ejecutarOperacion(
                    movimiento,
                    "RETIRO"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new ResultadoOperacionCajero(
                    false,
                    "Ocurrió un error al registrar el retiro.",
                    null
            );
        }
    }

    private String validarMovimiento(Movimiento movimiento) {

        if (movimiento == null) {
            return "Los datos de la operación son obligatorios.";
        }

        if (movimiento.getCuentaId() == null) {
            return "Debe indicar la cuenta.";
        }

        if (movimiento.getMonto() == null) {
            return "Debe indicar el monto.";
        }

        if (movimiento.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return "El monto debe ser mayor que cero.";
        }

        return null;
    }
}
