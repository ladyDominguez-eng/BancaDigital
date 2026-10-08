package com.proyecto.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase para manejar la conexión a la base de datos PostgreSQL (Singleton)
public class ConexionBD {
    // Instancia única
    private static ConexionBD instancia;
    private Connection conexion;

    // Constructor privado
    private ConexionBD() {
        String[] urls = {
            "jdbc:postgresql://localhost:5432/banca_bd",
            "jdbc:postgresql://localhost:5432/Banca_BD"
        };
        String usuario = "postgres";
        String envPass = System.getenv("DB_PASSWORD");
        String[] claves = {
            envPass,
            "Fran2020",
            "aaa",
            "system",
            "postgres",
            "123456",
            "admin"
        };

        boolean conecto = false;
        for (String url : urls) {
            for (String clave : claves) {
                if (clave == null) continue;
                try {
                    conexion = DriverManager.getConnection(url, usuario, clave);
                    System.out.println("Conexión establecida con PostgreSQL (" + url + ")");
                    conecto = true;
                    break;
                } catch (SQLException ignored) {
                }
            }
            if (conecto) break;
        }

        if (!conecto) {
            System.err.println("Error: No se pudo conectar a PostgreSQL con ninguna de las credenciales configuradas.");
        }
    }

    // Método para obtener la instancia única
    public static ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    // Retorna la conexión
    public Connection getConexion() {
        return conexion;
    }
}
