package com.proyecto.seguridad;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PATRÓN: Singleton (Creacional).
 *
 * PROBLEMA: el login solo devolvía {success, rol}. Ningún endpoint sabía
 *           quién llamaba, así que cualquiera podía invocar /api/usuarios o
 *           ver las cuentas de otro cliente (incumple RNF-01).
 * SOLUCIÓN: al iniciar sesión se crea un token aleatorio y se guarda aquí
 *           junto a los datos del usuario. Todas las peticiones traen ese
 *           token y el AuthInterceptor lo valida contra este registro.
 * POR QUÉ SINGLETON: el registro de sesiones tiene que ser UNO solo y
 *           compartido por todo el servidor; si cada clase creara el suyo,
 *           un token creado en el login no sería reconocido en otro lado.
 * POR QUÉ NO OTRO: Factory crearía registros distintos. Se descartó JWT
 *           firmado porque exige una librería nueva y gestionar claves; para
 *           el alcance del curso un token opaco en memoria es suficiente.
 * LIMITACIÓN: las sesiones viven en memoria; si el servidor se reinicia
 *           (por ejemplo en Render) hay que volver a iniciar sesión.
 */
public class GestorSesiones {

    // Duración máxima de una sesión
    private static final Duration DURACION = Duration.ofHours(8);

    private final SecureRandom aleatorio = new SecureRandom();
    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

    private GestorSesiones() { }

    // Clase interna estática: crea la instancia única de forma segura
    private static class Holder {
        private static final GestorSesiones INSTANCIA = new GestorSesiones();
    }

    public static GestorSesiones getInstancia() {
        return Holder.INSTANCIA;
    }

    /** Crea una sesión nueva y devuelve su token. */
    public String crear(Sesion sesion) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, sesion);
        return token;
    }

    /** Devuelve la sesión del token, o null si no existe o ya venció. */
    public Sesion obtener(String token) {
        if (token == null) {
            return null;
        }
        Sesion s = sesiones.get(token);
        if (s != null && Instant.now().isAfter(s.creada().plus(DURACION))) {
            sesiones.remove(token);
            return null;
        }
        return s;
    }

    public void cerrar(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }
}
