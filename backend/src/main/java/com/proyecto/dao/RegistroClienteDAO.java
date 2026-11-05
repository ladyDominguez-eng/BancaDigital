package com.proyecto.dao;

import com.proyecto.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Alta atómica de un cliente: usuario + cliente + cuenta en una transacción.
 *
 * PROBLEMA: si se crearan por separado (tres llamadas) y la segunda fallara,
 *           quedaría un usuario CLIENTE sin datos de cliente ni cuenta: no
 *           podría hacer nada y el DNI o el usuario quedarían "ocupados".
 * SOLUCIÓN: ConexionBD.ejecutarTransaccion confirma todo junto o deshace todo
 *           (mismo mecanismo que usan los movimientos de dinero).
 */
public class RegistroClienteDAO implements IRegistroClienteDAO {

    // Formato de los números de cuenta existentes: 001-0000010
    private static final String PREFIJO = "001-";

    @Override
    public Alta registrar(String username, String passwordHash, String dni,
            String nombres, String apellidos, String tipoCuenta) throws SQLException {

        return ConexionBD.getInstancia().ejecutarTransaccion(con -> {
            int usuarioId = insertarYDevolverId(con,
                    "INSERT INTO usuario (username, password_hash, rol, activo) "
                            + "VALUES (?, ?, 'CLIENTE', true) RETURNING id",
                    username, passwordHash);

            int clienteId = insertarYDevolverId(con,
                    "INSERT INTO cliente (usuario_id, dni, nombres, apellidos) "
                            + "VALUES (" + usuarioId + ", ?, ?, ?) RETURNING id",
                    dni, nombres, apellidos);

            String numero = siguienteNumeroDeCuenta(con);
            int cuentaId = insertarYDevolverId(con,
                    "INSERT INTO cuenta (numero, tipo, saldo, estado, cliente_id) "
                            + "VALUES (?, ?, 0, 'ACTIVA', " + clienteId + ") RETURNING id",
                    numero, tipoCuenta);

            return new Alta(usuarioId, clienteId, cuentaId, numero);
        });
    }

    @Override
    public boolean existeDni(String dni) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM cliente WHERE dni = ?")) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // El número sale del mayor existente + 1, dentro de la misma transacción.
    // Los ids numéricos (usuarioId, clienteId) se concatenan porque son enteros
    // que devolvió la propia base, nunca texto del usuario.
    private String siguienteNumeroDeCuenta(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero FROM 5) AS INTEGER)), 0) + 1 "
                + "FROM cuenta WHERE numero ~ '^001-[0-9]+$'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format(PREFIJO + "%07d", rs.getInt(1));
        }
    }

    private int insertarYDevolverId(Connection con, String sql, String... parametros)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setString(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
