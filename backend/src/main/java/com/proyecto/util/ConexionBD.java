
package com.proyecto.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    //esto permitira que las clases puedan ejecutar consultas atravez de el.
    private Connection conexion;
    // Constructor privado para impedir que se use el new desde una clase externa obligando a que todos 
    //usen la misma instancia.
    private ConexionBD() {
        // Se intenta obtener la conexión a la base de datos usando las propiedades del sistema o variables de entorno.
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
            // Se valida que las variables de conexión no sean nulas o estén vacías.
            if (url == null || usuario == null || clave == null 
                    || url.isBlank() || usuario.isBlank() || clave.isBlank()) {
               // Si alguna de las variables de conexión es nula o está vacía, se lanza una excepción indicando que faltan las variables de conexión a Supabase.
                throw new IllegalStateException(
                    "Faltan las variables de conexión a Supabase"
                );
            }
            // Se establece la conexión a la base de datos usando JDBC.
            // Se utiliza DriverManager para obtener la conexión a la base de datos PostgreSQL.
            // Se imprime un mensaje indicando que la conexión JDBC se ha establecido correctamente.
            conexion = DriverManager.getConnection(url, usuario, clave);
            System.out.println("Conexión JDBC establecida con PostgreSQL");

        //se imprime un mensaje indicando que habido un error en la conexión JDBC y se lanza una excepción indicando el error.
        } catch (SQLException e) {
            throw new IllegalStateException(
                "Error en la conexión JDBC: " + e.getMessage(), e
            );
        }
    }
    // Clase interna estática para mantener la instancia única de ConexionBD.
    private static class ConexionBDHolder {
        // Se crea una instancia única de ConexionBD.
        private static final ConexionBD INSTANCE = new ConexionBD();
    }

    // Método público para obtener la instancia única de ConexionBD.
    public static ConexionBD getInstancia() {
        return ConexionBDHolder.INSTANCE;
    }
    // Método público para obtener la conexión a la base de datos. 
    public Connection getConexion() {
        return conexion;
    }
}
