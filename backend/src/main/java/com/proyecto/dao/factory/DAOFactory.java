package com.proyecto.dao.factory;

import com.proyecto.dao.CuentaDAO;
import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.ClienteDAO;
import com.proyecto.dao.IClienteDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.dao.TransaccionDAO;
import com.proyecto.dao.IInformesDAO;
import com.proyecto.dao.IParametroDAO;
import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.dao.InformesDAO;
import com.proyecto.dao.ParametroDAO;
import com.proyecto.dao.UsuarioDAO;

/**
 * PATRÓN: Factory (Creacional).
 *
 * PROBLEMA: cada servicio creaba su DAO con "new" y quedaba atado a la
 *           clase concreta (JDBC). Cambiar la implementación obligaba a
 *           modificar todos los servicios que la usaban.
 * SOLUCIÓN: los servicios piden el DAO a esta fábrica y lo reciben como
 *           interfaz (IUsuarioDAO, ICuentaDAO). Solo la fábrica conoce las
 *           clases concretas.
 * POR QUÉ NO OTRO: Singleton garantiza una única instancia, pero aquí lo
 *           que se necesita es ocultar QUÉ clase se crea. Abstract Factory
 *           sería excesivo: no hay familias de objetos que cambien juntas,
 *           solo un DAO por entidad.
 * SIN EL PATRÓN: cada "new CuentaDAO()" repartido por el código habría que
 *           cambiarlo a mano si la persistencia cambia.
 *
 * CORRECCIÓN: antes crearUsuarioDAO() hacía un cast (IUsuarioDAO) sobre
 * UsuarioDAO, que no implementaba la interfaz; habría fallado con
 * ClassCastException al ejecutarse. Ahora UsuarioDAO sí la implementa y el
 * cast ya no es necesario.
 */
public class DAOFactory {

    // Constructor privado: la fábrica solo ofrece métodos estáticos
    private DAOFactory() { }

    public static IUsuarioDAO crearUsuarioDAO() {
        return new UsuarioDAO();
    }

    public static ICuentaDAO crearCuentaDAO() {
        return new CuentaDAO();
    }

    // Se agregan los DAO que antes se creaban con "new" dentro de los
    // servicios (ParametroService e InformesService).
    public static IParametroDAO crearParametroDAO() {
        return new ParametroDAO();
    }

    public static IInformesDAO crearInformesDAO() {
        return new InformesDAO();
    }

    // DAO de los módulos Cliente, Cajero, Asesor y Auditor
    public static ITransaccionDAO crearTransaccionDAO() {
        return new TransaccionDAO();
    }

    public static IClienteDAO crearClienteDAO() {
        return new ClienteDAO();
    }
}
