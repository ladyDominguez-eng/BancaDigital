package com.proyecto.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Filtro de seguridad: antes de llegar a cualquier controlador comprueba el
 * token y el rol (RNF-01).
 *
 * PROBLEMA: los endpoints eran públicos; cualquiera podía listar usuarios o
 *           registrar depósitos sin iniciar sesión.
 * SOLUCIÓN: este interceptor responde 401 si falta o venció el token y 403
 *           si el rol no tiene permiso. Si todo está bien deja la Sesion en
 *           la petición para que el controlador sepa quién llama.
 * POR QUÉ NO OTRO: Spring Security haría lo mismo pero exige una
 *           configuración mucho mayor; el interceptor es la pieza mínima del
 *           propio Spring MVC y se entiende completa.
 */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_SESION = "sesion";

    private final GestorSesiones sesiones;

    public AuthInterceptor(GestorSesiones sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {

        // Las consultas previas de CORS (OPTIONS) no llevan token
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        String ruta = req.getRequestURI();
        if (ReglasAcceso.esPublica(ruta)) {
            return true;
        }

        Sesion sesion = sesiones.obtener(extraerToken(req));
        if (sesion == null) {
            return rechazar(res, 401, "Sesión no válida o vencida. Inicia sesión.");
        }

        Set<String> permitidos = ReglasAcceso.rolesPermitidos(ruta);
        if (!permitidos.isEmpty() && !permitidos.contains(sesion.rol())) {
            return rechazar(res, 403, "No tienes permiso para esta operación.");
        }

        req.setAttribute(ATRIBUTO_SESION, sesion);
        return true;
    }

    public static String extraerToken(HttpServletRequest req) {
        String cabecera = req.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            return cabecera.substring(7).trim();
        }
        return null;
    }

    private boolean rechazar(HttpServletResponse res, int codigo, String mensaje)
            throws IOException {
        res.setStatus(codigo);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\":\"" + mensaje + "\"}");
        return false;
    }
}
