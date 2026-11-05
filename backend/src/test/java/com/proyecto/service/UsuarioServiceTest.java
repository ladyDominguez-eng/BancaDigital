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

    @Test
    void rolIncorrectoNoEntra() {
        DaoFalso dao = new DaoFalso();
        dao.usuario = usuarioConHash(new BCryptPasswordEncoder(10).encode("clave123"));
        UsuarioService servicio = new UsuarioService(dao, evento -> { });

        assertFalse(servicio.validarUsuario("cajero1", "clave123", "ADMIN"));
    }
}
