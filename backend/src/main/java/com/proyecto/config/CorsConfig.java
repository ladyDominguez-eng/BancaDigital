package com.proyecto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración única de CORS para toda la API.
 *
 * PROBLEMA: @CrossOrigin con la misma lista de orígenes estaba copiado en
 *           6 controladores, y el de login usaba "*" (cualquier sitio web
 *           podía llamarlo). Cambiar la URL del frontend obligaba a editar
 *           todos los archivos y era fácil olvidar alguno.
 * SOLUCIÓN: un solo lugar que aplica la regla a todos los endpoints /api/**.
 *           Antipatrón corregido: copiar y pegar.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Orígenes permitidos: desarrollo local y frontend desplegado en Render
    private static final String[] ORIGENES_PERMITIDOS = {
        "http://localhost:4200",
        "https://bancadigital-frontend.onrender.com"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(ORIGENES_PERMITIDOS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
