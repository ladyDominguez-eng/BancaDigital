package com.proyecto;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.model.Usuario;
import com.proyecto.seguridad.GestorSesiones;
import com.proyecto.seguridad.Sesion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueba de integración de SOLO LECTURA contra la base de datos real.
 *
 * Arranca toda la aplicación (comprueba que todos los beans se crean y se
 * conectan) y llama a los endpoints de consulta de cada rol con un token
 * válido. No crea ni modifica datos. Solo se ejecuta si existe la variable
 * de entorno SPRING_DATASOURCE_PASSWORD; sin ella se omite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_PASSWORD", matches = ".+")
class IntegracionLecturaTest {

    @Autowired MockMvc mvc;
    @Autowired IUsuarioDAO usuarios;

    // Token de un usuario real con ese rol (activo o no: el token se crea sin pasar por el login)
    private String token(String rol) {
        Usuario u = usuarios.listarTodos().stream()
                .filter(x -> x.getRol().equals(rol))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hay ningún usuario con rol " + rol));
        return "Bearer " + GestorSesiones.getInstancia().crear(new Sesion(u.getId(), u.getUsername(), rol));
    }

    private MvcResult ok(String ruta, String rol) throws Exception {
        return mvc.perform(get(ruta).header("Authorization", token(rol)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void sinTokenOConRolIncorrectoSeRechaza() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios").header("Authorization", token("CAJERO")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/asesor/solicitudes").header("Authorization", token("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorYInformes() throws Exception {
        String json = ok("/api/usuarios", "ADMIN").getResponse().getContentAsString();
        assertFalse(json.contains("passwordHash"), "el hash no debe salir en el JSON");
        ok("/api/informes/cuentas", "ADMIN");
        ok("/api/informes/cuentas", "AUDITOR"); // el auditor también ve informes
        ok("/api/auditoria", "AUDITOR");        // y la bitácora de accesos
    }

    @Test
    void cliente() throws Exception {
        String cuentas = ok("/api/cliente/cuentas", "CLIENTE").getResponse().getContentAsString();
        assertTrue(cuentas.startsWith("["));
        ok("/api/cliente/creditos", "CLIENTE");
        ok("/api/cliente/servicios", "CLIENTE");
    }

    @Test
    void clienteNoPuedeVerMovimientosDeUnaCuentaAjena() throws Exception {
        mvc.perform(get("/api/cliente/cuentas/-1/movimientos").header("Authorization", token("CLIENTE")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cajero() throws Exception {
        ok("/api/cajero/cuentas?dni=00000000", "CAJERO");
        mvc.perform(get("/api/cajero/comprobantes/NO-EXISTE").header("Authorization", token("CAJERO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asesor() throws Exception {
        ok("/api/asesor/solicitudes", "ASESOR");
        ok("/api/asesor/solicitudes?estado=PENDIENTE", "ASESOR");
        ok("/api/asesor/clientes/-1/historial", "ASESOR");
        mvc.perform(get("/api/asesor/solicitudes/-1").header("Authorization", token("ASESOR")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/asesor/solicitudes?estado=INVENTADO").header("Authorization", token("ASESOR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void auditor() throws Exception {
        ok("/api/auditor/transacciones", "AUDITOR");
        ok("/api/auditor/transacciones?tipo=TRANSFERENCIA&montoMinimo=1", "AUDITOR");
        ok("/api/auditor/riesgo", "AUDITOR");
        mvc.perform(get("/api/auditor/transacciones?desde=hola").header("Authorization", token("AUDITOR")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/auditor/exportar?formato=pdf").header("Authorization", token("AUDITOR")))
                .andExpect(status().isBadRequest());
    }
}
