package com.proyecto.util;

/**
 * Error de acceso a datos (fallo SQL o de conexión).
 *
 * PROBLEMA: UsuarioDAO capturaba SQLException, imprimía el error en consola
 *           y devolvía null/false/lista vacía. Si la base fallaba, el login
 *           respondía "credenciales inválidas" y el listado salía vacío:
 *           el error quedaba oculto (antipatrón "tragarse excepciones").
 * SOLUCIÓN: el DAO lanza esta excepción y la capa superior responde con un
 *           error 500 real, sin confundirlo con un resultado normal.
 */
public class AccesoDatosException extends RuntimeException {

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
