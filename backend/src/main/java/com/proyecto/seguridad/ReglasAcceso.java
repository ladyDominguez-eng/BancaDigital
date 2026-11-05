package com.proyecto.seguridad;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tabla única de "qué rol puede entrar a qué ruta" (RNF-01).
 *
 * PROBLEMA: sin esta tabla habría que repetir la comprobación de rol dentro
 *           de cada controlador (copiar y pegar, y fácil de olvidar una).
 * SOLUCIÓN: las reglas viven aquí y las aplica el AuthInterceptor antes de
 *           llegar al controlador. Para proteger una ruta nueva basta una
 *           línea (O de SOLID: se amplía sin tocar el interceptor).
 */
public final class ReglasAcceso {

    // Rutas que no necesitan sesión
    private static final Set<String> PUBLICAS = Set.of("/api/login", "/api/logout");

    // Prefijo de ruta -> roles permitidos (se evalúa en orden)
    private static final Map<String, Set<String>> POR_PREFIJO = new LinkedHashMap<>();
    static {
        POR_PREFIJO.put("/api/usuarios",   Set.of("ADMIN"));
        POR_PREFIJO.put("/api/parametros", Set.of("ADMIN"));
        POR_PREFIJO.put("/api/informes",   Set.of("ADMIN", "AUDITOR"));
        POR_PREFIJO.put("/api/auditoria",  Set.of("ADMIN", "AUDITOR"));
        POR_PREFIJO.put("/api/cajero",     Set.of("CAJERO"));
        POR_PREFIJO.put("/api/cliente",    Set.of("CLIENTE"));
        POR_PREFIJO.put("/api/asesor",     Set.of("ASESOR"));
        POR_PREFIJO.put("/api/auditor",    Set.of("AUDITOR"));
    }

    private ReglasAcceso() { }

    public static boolean esPublica(String ruta) {
        return PUBLICAS.contains(ruta);
    }

    /**
     * Roles permitidos para la ruta. Si la ruta no está en la tabla se
     * exige solo estar autenticado (conjunto vacío = cualquier rol).
     */
    public static Set<String> rolesPermitidos(String ruta) {
        for (Map.Entry<String, Set<String>> regla : POR_PREFIJO.entrySet()) {
            String prefijo = regla.getKey();
            if (ruta.equals(prefijo) || ruta.startsWith(prefijo + "/")) {
                return regla.getValue();
            }
        }
        return Set.of();
    }
}
