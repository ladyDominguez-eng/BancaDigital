package com.proyecto.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// Clase para manejar la conexión a la base de datos PostgreSQL, (Singleton )
public class ConexionBD {
    // Instancia única
    private static ConexionBD instancia;
    private Connection conexion;

    // Constructor privado
    private ConexionBD() {
        try {
            String url = "jdbc:postgresql://localhost:5432/Banca_BD";
            String usuario = "postgres";
            String clave = "system"; 

            conexion = DriverManager.getConnection(url, usuario, clave);
            System.out.println("Conexión establecida con PostgreSQL");
        } catch (SQLException e) {
            System.err.println("Error al conectar: " + e.getMessage());
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

