package com.proyecto.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** Pruebas de las reglas de acceso y del gestor de sesiones (sin base de datos). */
class SeguridadTest {

    @Test
    void cadaRutaTieneSusRoles() {
        assertEquals(Set.of("ADMIN"), ReglasAcceso.rolesPermitidos("/api/usuarios"));
        assertEquals(Set.of("ADMIN"), ReglasAcceso.rolesPermitidos("/api/usuarios/5/estado"));
        assertEquals(Set.of("CAJERO"), ReglasAcceso.rolesPermitidos("/api/cajero/depositos"));
        assertEquals(Set.of("CLIENTE"), ReglasAcceso.rolesPermitidos("/api/cliente/cuentas"));
        assertEquals(Set.of("ADMIN", "AUDITOR"), ReglasAcceso.rolesPermitidos("/api/informes/cuentas"));
    }

    @Test
    void unaRutaNoListadaSoloExigeEstarAutenticado() {
        assertTrue(ReglasAcceso.rolesPermitidos("/api/otra").isEmpty());
    }

    @Test
    void unPrefijoParecidoNoCoincide() {
        // "/api/clientes" no debe confundirse con "/api/cliente"
        assertTrue(ReglasAcceso.rolesPermitidos("/api/clientes").isEmpty());
    }

    @Test
    void soloLoginYLogoutSonPublicas() {
        assertTrue(ReglasAcceso.esPublica("/api/login"));
        assertTrue(ReglasAcceso.esPublica("/api/logout"));
        assertFalse(ReglasAcceso.esPublica("/api/usuarios"));
    }

    @Test
    void lasSesionesSeCreanYSeCierran() {
        GestorSesiones g = GestorSesiones.getInstancia();
        String token = g.crear(new Sesion(7, "ana", "CLIENTE"));

        assertNotNull(g.obtener(token));
        assertEquals("CLIENTE", g.obtener(token).rol());

        g.cerrar(token);
        assertNull(g.obtener(token));
        assertNull(g.obtener("token-inventado"));
        assertNull(g.obtener(null));
    }
}
