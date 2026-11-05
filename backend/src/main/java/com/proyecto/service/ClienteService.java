package com.proyecto.service;

import com.proyecto.dao.IClienteDAO;
import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.model.Cliente;
import com.proyecto.model.Cuenta;
import com.proyecto.model.TransaccionVista;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/** Consultas del cliente sobre sus propias cuentas y movimientos (RF-04). */
@Service
public class ClienteService {

    private final ICuentaDAO cuentas;
    private final ITransaccionDAO transacciones;
    private final IClienteDAO clientes;

    public ClienteService(ICuentaDAO cuentas, ITransaccionDAO transacciones, IClienteDAO clientes) {
        this.cuentas = cuentas;
        this.transacciones = transacciones;
        this.clientes = clientes;
    }

    public List<Cuenta> misCuentas(int usuarioId) throws SQLException {
        return cuentas.listarPorUsuario(usuarioId);
    }

    public List<TransaccionVista> movimientos(int usuarioId, int cuentaId) throws SQLException {
        // Solo se muestran movimientos de cuentas que son del propio usuario
        if (cuentas.buscarDeUsuario(cuentaId, usuarioId) == null) {
            throw new IllegalArgumentException("La cuenta seleccionada no existe.");
        }
        return transacciones.listarPorCuenta(cuentaId);
    }

    /** Cliente asociado al usuario; error claro si el usuario no tiene ficha de cliente. */
    public Cliente clienteDe(int usuarioId) throws SQLException {
        Cliente c = clientes.buscarPorUsuarioId(usuarioId);
        if (c == null) {
            throw new IllegalArgumentException("Tu usuario no tiene datos de cliente registrados.");
        }
        return c;
    }
}
