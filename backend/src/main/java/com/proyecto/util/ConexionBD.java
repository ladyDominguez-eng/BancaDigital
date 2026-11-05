
package com.proyecto.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PATRÓN: Singleton (Creacional).
 *
 * PROBLEMA: sin control, cada DAO abriría su propia conexión a Supabase,
 *           desperdiciando recursos y arriesgando fugas.
 * SOLUCIÓN: una única instancia de esta clase guarda la configuración y la
 *           conexión; todos los DAO la piden aquí con getInstancia().
 * POR QUÉ NO OTRO: Factory crearía una conexión nueva en cada llamada, que
 *           es justo lo que se quiere evitar. Aquí hace falta garantizar
 *           UNA sola instancia compartida.
 * SIN EL PATRÓN: conexiones repartidas por el código, sin punto único donde
 *           cerrarlas ni configurarlas.
 *
 * MEJORAS DE ESTA VERSIÓN
 * 1) Reconexión: antes, si la conexión se cerraba (Supabase corta las
 *    inactivas), todas las operaciones fallaban hasta reiniciar el
 *    servidor. getConexion() ahora comprueba la conexión y la reabre.
 * 2) Transacciones: antes CuentaDAO cambiaba el autocommit sobre la
 *    conexión compartida sin protegerla; dos peticiones a la vez podían
 *    mezclar sus operaciones. ejecutarTransaccion() serializa el acceso y
 *    centraliza commit/rollback, evitando repetir ese código en cada DAO
 *    (antipatrón "copiar y pegar").
 *
 * LIMITACIÓN CONOCIDA: una sola conexión compartida no escala. En un
 * sistema real el Singleton debería envolver un pool de conexiones
 * (por ejemplo HikariCP). Se deja como recomendación futura.
 */
public class ConexionBD {

    // Datos de conexión guardados para poder reabrirla si se cae.
    private final String url;
    private final String usuario;
    private final String clave;

    //esto permitira que las clases puedan ejecutar consultas atravez de el.
    private Connection conexion;

    /** Trabajo que se ejecuta dentro de una transacción. */
    @FunctionalInterface
    public interface TrabajoTransaccional<T> {
        T ejecutar(Connection con) throws SQLException;
    }

    // Constructor privado para impedir que se use el new desde una clase externa obligando a que todos
    //usen la misma instancia.
    private ConexionBD() {
        // Se obtienen los datos usando las propiedades del sistema o variables de entorno.
        String u = System.getProperty("spring.datasource.url");
        String us = System.getProperty("spring.datasource.username");
        String c = System.getProperty("spring.datasource.password");

        // Si Spring no las expone como propiedades del sistema,
        // se leen directamente de las variables de entorno.
        if (u == null || u.isBlank()) {
            u = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (us == null || us.isBlank()) {
            us = System.getenv("SPRING_DATASOURCE_USERNAME");
        }
        if (c == null || c.isBlank()) {
            c = System.getenv("SPRING_DATASOURCE_PASSWORD");
        }
        // Se valida que las variables de conexión no sean nulas o estén vacías.
        if (u == null || us == null || c == null
                || u.isBlank() || us.isBlank() || c.isBlank()) {
            throw new IllegalStateException(
                "Faltan las variables de conexión a Supabase"
            );
        }

        this.url = u;
        this.usuario = us;
        this.clave = c;
        conectar();
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

    // Abre la conexión JDBC con PostgreSQL (se usa al inicio y al reconectar).
    private void conectar() {
        try {
            conexion = DriverManager.getConnection(url, usuario, clave);
            System.out.println("Conexión JDBC establecida con PostgreSQL");
        } catch (SQLException e) {
            throw new IllegalStateException(
                "Error en la conexión JDBC: " + e.getMessage(), e
            );
        }
    }

    /** Devuelve la conexión; si está cerrada o caída, la vuelve a abrir. */
    public synchronized Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
                conectar();
            }
        } catch (SQLException e) {
            conectar();
        }
        return conexion;
    }

    /**
     * Ejecuta un trabajo dentro de una transacción: confirma (commit) si
     * termina bien y deshace (rollback) si falla. Mientras dura, ningún
     * otro hilo obtiene la conexión.
     */
    public synchronized <T> T ejecutarTransaccion(TrabajoTransaccional<T> trabajo)
            throws SQLException {
        Connection con = getConexion();
        boolean autoCommitOriginal = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            T resultado = trabajo.ejecutar(con);
            con.commit();
            return resultado;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitOriginal);
        }
    }
}
