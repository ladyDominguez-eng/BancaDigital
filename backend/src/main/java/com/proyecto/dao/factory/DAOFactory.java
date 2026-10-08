package com.proyecto.dao.factory;

import com.proyecto.dao.CuentaDAO;
import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.dao.UsuarioDAO;

/**
 * Patrón Factory (creacional): crea el DAO correcto sin que
 * el Service conozca la clase concreta.
 */
public class DAOFactory {

    private DAOFactory() { }

    public static IUsuarioDAO crearUsuarioDAO() {
        return (IUsuarioDAO) new UsuarioDAO();
    }

    public static ICuentaDAO crearCuentaDAO() {
        return new CuentaDAO();
    }
}