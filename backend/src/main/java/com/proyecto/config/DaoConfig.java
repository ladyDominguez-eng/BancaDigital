package com.proyecto.config;

import com.proyecto.dao.ICuentaDAO;
import com.proyecto.dao.IClienteDAO;
import com.proyecto.dao.IParametroDAO;
import com.proyecto.dao.ISolicitudCreditoDAO;
import com.proyecto.dao.ITransaccionDAO;
import com.proyecto.dao.factory.DAOFactory;
import com.proyecto.patterns.AuditoriaPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Entrega a Spring los DAO que crea la DAOFactory.
 *
 * PROBLEMA: los servicios nuevos necesitan sus DAO, pero crearlos con "new"
 *           dentro del servicio los ata a la clase concreta y no se pueden
 *           probar sin base de datos.
 * SOLUCIÓN: la Factory sigue siendo la única que conoce las clases
 *           concretas; Spring recibe sus resultados como beans y los inyecta
 *           por constructor en los servicios (D de SOLID).
 */
@Configuration
public class DaoConfig {

    @Bean
    public ICuentaDAO cuentaDAO() {
        return DAOFactory.crearCuentaDAO();
    }

    @Bean
    public ITransaccionDAO transaccionDAO() {
        return DAOFactory.crearTransaccionDAO();
    }

    @Bean
    public IClienteDAO clienteDAO() {
        return DAOFactory.crearClienteDAO();
    }

    @Bean
    public ISolicitudCreditoDAO solicitudCreditoDAO() {
        return DAOFactory.crearSolicitudCreditoDAO();
    }

    @Bean
    public IParametroDAO parametroDAO() {
        return DAOFactory.crearParametroDAO();
    }

    // El sujeto del Observer (Singleton) también se inyecta, en vez de
    // llamarlo desde dentro de cada servicio.
    @Bean
    public AuditoriaPublisher auditoriaPublisher() {
        return AuditoriaPublisher.getInstancia();
    }
}
