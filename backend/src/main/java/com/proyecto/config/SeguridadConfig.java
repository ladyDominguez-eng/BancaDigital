package com.proyecto.config;

import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.GestorSesiones;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Activa el AuthInterceptor sobre toda la API. */
@Configuration
public class SeguridadConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(GestorSesiones.getInstancia()))
                .addPathPatterns("/api/**");
    }
}
