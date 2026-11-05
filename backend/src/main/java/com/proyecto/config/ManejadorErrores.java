package com.proyecto.config;

import com.proyecto.util.AccesoDatosException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones a respuestas HTTP en un solo lugar.
 *
 * PROBLEMA: cada controlador repetía el mismo try/catch
 *           (IllegalArgumentException -> 400, otra cosa -> 500) y además
 *           el error real se perdía: nunca se registraba en el servidor.
 * SOLUCIÓN: este manejador global atiende todos los controladores. Los
 *           controladores quedan solo con su trabajo de coordinar (GRASP
 *           Controlador) y los errores se registran en el log.
 * Antipatrón corregido: copiar y pegar.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    // Datos inválidos que envía el cliente -> 400
    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    public ResponseEntity<Map<String, String>> datosInvalidos(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    // Fallos de base de datos -> 500 con mensaje genérico (el detalle va al log)
    @ExceptionHandler({AccesoDatosException.class, SQLException.class})
    public ResponseEntity<Map<String, String>> errorDeDatos(Exception e) {
        log.error("Error de acceso a datos", e);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al acceder a los datos."));
    }

    // Cualquier otro error inesperado -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> errorInesperado(Exception e) {
        log.error("Error inesperado", e);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error inesperado en el servidor."));
    }
}
