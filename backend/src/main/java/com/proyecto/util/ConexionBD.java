
package com.proyecto.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static ConexionBD instancia;
    private Connection conexion;

    private ConexionBD() {
        try {
            String url = System.getProperty("spring.datasource.url");
            String usuario = System.getProperty("spring.datasource.username");
            String clave = System.getProperty("spring.datasource.password");

            // Si Spring no las expone como propiedades del sistema,
            // se leen directamente de las variables de entorno.
            if (url == null || url.isBlank()) {
                url = System.getenv("SPRING_DATASOURCE_URL");
            }
            if (usuario == null || usuario.isBlank()) {
                usuario = System.getenv("SPRING_DATASOURCE_USERNAME");
            }
            if (clave == null || clave.isBlank()) {
                clave = System.getenv("SPRING_DATASOURCE_PASSWORD");
            }

            if (url == null || usuario == null || clave == null
                    || url.isBlank() || usuario.isBlank() || clave.isBlank()) {
                throw new IllegalStateException(
                    "Faltan las variables de conexión a Supabase"
                );
            }

            conexion = DriverManager.getConnection(url, usuario, clave);
            System.out.println("Conexión JDBC establecida con PostgreSQL");

        } catch (SQLException e) {
            throw new IllegalStateException(
                "Error en la conexión JDBC: " + e.getMessage(), e
            );
        }
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }
}
