package com.proyecto.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Pruebas del AuthInterceptor con peticiones simuladas (sin servidor ni base de datos). */
class AuthInterceptorTest {

    private final GestorSesiones sesiones = GestorSesiones.getInstancia();
    private final AuthInterceptor interceptor = new AuthInterceptor(sesiones);

    private MockHttpServletRequest peticion(String metodo, String ruta, String token) {
        MockHttpServletRequest req = new MockHttpServletRequest(metodo, ruta);
        req.setRequestURI(ruta);
        if (token != null) {
            req.addHeader("Authorization", "Bearer " + token);
        }
        return req;
    }

    @Test
    void sinTokenResponde401() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean pasa = interceptor.preHandle(peticion("GET", "/api/usuarios", null), res, null);

        assertFalse(pasa);
        assertEquals(401, res.getStatus());
    }

    @Test
    void rolIncorrectoResponde403() throws Exception {
        String token = sesiones.crear(new Sesion(1, "cajero1", "CAJERO"));
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean pasa = interceptor.preHandle(peticion("GET", "/api/usuarios", token), res, null);

        assertFalse(pasa);
        assertEquals(403, res.getStatus());
    }

    @Test
    void rolCorrectoPasaYDejaLaSesionEnLaPeticion() throws Exception {
        String token = sesiones.crear(new Sesion(2, "ana", "CLIENTE"));
        MockHttpServletRequest req = peticion("GET", "/api/cliente/cuentas", token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean pasa = interceptor.preHandle(req, res, null);

        assertTrue(pasa);
        Sesion s = (Sesion) req.getAttribute(AuthInterceptor.ATRIBUTO_SESION);
        assertEquals(2, s.usuarioId());
    }

    @Test
    void loginYOptionsNoPidenToken() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(peticion("POST", "/api/login", null), res, null));
        assertTrue(interceptor.preHandle(peticion("OPTIONS", "/api/usuarios", null), res, null));
    }
}
