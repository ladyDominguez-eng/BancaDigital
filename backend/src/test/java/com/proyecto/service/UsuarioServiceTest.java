package com.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;

import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.model.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Prueba de UsuarioService con un DAO falso en memoria.
 *
 * Esto es posible gracias a D de SOLID: el servicio depende de la interfaz
 * IUsuarioDAO, así que se puede reemplazar por un falso sin base de datos.
 */
class UsuarioServiceTest {

    /** DAO falso: guarda un solo usuario y anota si le cambiaron el hash. */
    static class DaoFalso implements IUsuarioDAO {
        Usuario usuario;
        String hashGuardado;

        @Override public Usuario buscarPorUsername(String username) { return usuario; }
        @Override public List<Usuario> listarTodos() { return List.of(usuario); }
        @Override public Usuario crear(Usuario u) { return u; }
        @Override public Usuario buscarPorId(Long id) { return usuario; }
        @Override public boolean cambiarRol(Long id, String rol) { return true; }
        @Override public boolean cambiarEstado(Long id, boolean activo) { return true; }
        @Override public boolean actualizarPasswordHash(Long id, String nuevoHash) {
            hashGuardado = nuevoHash;
            return true;
        }
    }

    private Usuario usuarioConHash(String hash) {
        Usuario u = new Usuario();
        u.setId(1);
        u.setUsername("cajero1");
        u.setRol("CAJERO");
        u.setActivo(true);
        u.setPasswordHash(hash);
        return u;
    }

    @Test
    void loginCorrectoMejoraUnHashDebil() {
        DaoFalso dao = new DaoFalso();
        dao.usuario = usuarioConHash(new BCryptPasswordEncoder(6).encode("clave123"));
        UsuarioService servicio = new UsuarioService(dao, evento -> { });

        assertTrue(servicio.validarUsuario("cajero1", "clave123", "CAJERO"));

        assertNotNull(dao.hashGuardado, "debe guardar un hash nuevo");
        assertTrue(dao.hashGuardado.startsWith("$2a$10$"), "con costo 10");
        assertTrue(new BCryptPasswordEncoder().matches("clave123", dao.hashGuardado),
                "el hash nuevo debe validar la misma contraseña");
    }

    @Test
    void loginCorrectoNoTocaUnHashFuerte() {
        DaoFalso dao = new DaoFalso();
        dao.usuario = usuarioConHash(new BCryptPasswordEncoder(10).encode("clave123"));
        UsuarioService servicio = new UsuarioService(dao, evento -> { });

        assertTrue(servicio.validarUsuario("cajero1", "clave123", "CAJERO"));
        assertNull(dao.hashGuardado, "un hash ya fuerte no se reescribe");
    }

    @Test
    void contrasenaIncorrectaNoEntraNiCambiaElHash() {
        DaoFalso dao = new DaoFalso();
        dao.usuario = usuarioConHash(new BCryptPasswordEncoder(6).encode("clave123"));
        UsuarioService servicio = new UsuarioService(dao, evento -> { });

        assertFalse(servicio.validarUsuario("cajero1", "otra", "CAJERO"));
        assertNull(dao.hashGuardado);
    }

    // ---- Crear usuario del personal y cambiar rol ----

    private UsuarioService servicioConUsuarioExistente(Usuario existente) {
        DaoFalso dao = new DaoFalso() {
            @Override public Usuario buscarPorUsername(String username) { return existente; }
        };
        dao.usuario = existente;
        return new UsuarioService(dao, evento -> { });
    }

    @Test
    void crearUsuarioConDatosValidosFunciona() {
        UsuarioService servicio = servicioConUsuarioExistente(null);
        Usuario creado = servicio.crearUsuario(1, "nuevo_cajero", "Clave1234", "cajero");
        assertEquals("CAJERO", creado.getRol(), "el rol se guarda en mayúsculas");
        assertTrue(new BCryptPasswordEncoder().matches("Clave1234", creado.getPasswordHash()));
    }

    @Test
    void crearUsuarioRechazaDatosInvalidos() {
        UsuarioService servicio = servicioConUsuarioExistente(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.crearUsuario(1, "ab", "Clave1234", "CAJERO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearUsuario(1, "con espacio", "Clave1234", "CAJERO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearUsuario(1, "usuario1", "corta", "CAJERO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearUsuario(1, "usuario1", "Clave1234", "JEFE"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearUsuario(1, "usuario1", "Clave1234", null));
    }

    @Test
    void crearUsuarioNoPermiteRolClienteNiRepetidos() {
        assertThrows(IllegalArgumentException.class,
                () -> servicioConUsuarioExistente(null).crearUsuario(1, "usuario1", "Clave1234", "CLIENTE"));
        assertThrows(IllegalArgumentException.class,
                () -> servicioConUsuarioExistente(usuarioConHash("x")).crearUsuario(1, "cajero1", "Clave1234", "CAJERO"));
    }

    @Test
    void cambiarRolDeUnUsuarioDelPersonalFunciona() {
        Usuario u = usuarioConHash("x");           // id 1, CAJERO
        Usuario resultado = servicioConUsuarioExistente(u).cambiarRol(99, 1L, "auditor");
        assertEquals("AUDITOR", resultado.getRol());
    }

    @Test
    void cambiarRolTieneProtecciones() {
        Usuario u = usuarioConHash("x");           // id 1, CAJERO
        // nadie cambia su propio rol
        assertThrows(IllegalArgumentException.class, () -> servicioConUsuarioExistente(u).cambiarRol(1, 1L, "AUDITOR"));
        // no se pasa a CLIENTE
        assertThrows(IllegalArgumentException.class, () -> servicioConUsuarioExistente(u).cambiarRol(99, 1L, "CLIENTE"));
        // no se cambia el rol de un cliente
        u.setRol("CLIENTE");
        assertThrows(IllegalArgumentException.class, () -> servicioConUsuarioExistente(u).cambiarRol(99, 1L, "CAJERO"));
        // rol inexistente y usuario inexistente
        assertThrows(IllegalArgumentException.class, () -> servicioConUsuarioExistente(usuarioConHash("x")).cambiarRol(99, 1L, "JEFE"));
        assertThrows(IllegalArgumentException.class, () -> servicioConUsuarioExistente(null).cambiarRol(99, 1L, "CAJERO"));
    }

    @Test
    void rolIncorrectoNoEntra() {
        DaoFalso dao = new DaoFalso();
        dao.usuario = usuarioConHash(new BCryptPasswordEncoder(10).encode("clave123"));
        UsuarioService servicio = new UsuarioService(dao, evento -> { });

        assertFalse(servicio.validarUsuario("cajero1", "clave123", "ADMIN"));
    }
}
