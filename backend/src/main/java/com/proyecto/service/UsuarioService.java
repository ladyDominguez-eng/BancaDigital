package com.proyecto.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;
import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.model.Usuario;
import java.util.List;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;

@Service
public class UsuarioService {

    // Costo de BCrypt. Antes se usaba el valor por defecto sin dejarlo
    // explícito; ahora es una constante con nombre (antipatrón "valores
    // mágicos"). 10 es el mínimo recomendado hoy.
    private static final int COSTO_BCRYPT = 10;

    // D de SOLID: el servicio depende de la interfaz IUsuarioDAO. Se recibe
    // por constructor (en vez de @Autowired en el campo) para que sea
    // evidente la dependencia y se pueda probar con un DAO falso.
    private final IUsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(COSTO_BCRYPT);

    // Quién recibe los eventos de auditoría. Se inyecta para no llamar al
    // Singleton AuditoriaPublisher desde dentro del método (acoplamiento
    // oculto que impedía probar el servicio sin base de datos).
    private final Consumer<EventoAuditoria> auditor;

    // Constructor que usa Spring: la auditoría real es el Observer existente.
    @Autowired
    public UsuarioService(IUsuarioDAO usuarioDAO) {
        this(usuarioDAO, AuditoriaPublisher.getInstancia()::publicar);
    }

    // Constructor para pruebas: se pasa un auditor falso.
    public UsuarioService(IUsuarioDAO usuarioDAO, Consumer<EventoAuditoria> auditor) {
        this.usuarioDAO = usuarioDAO;
        this.auditor = auditor;
    }

    // Capa de negocio: el controlador pide la lista al servicio y ya no
    // accede al DAO directamente (se respeta Controller -> Service -> DAO).
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listarTodos();
    }

    // Método para validar usuario
    public boolean validarUsuario(String username, String password, String rol) {

        Usuario usuario = usuarioDAO.buscarPorUsername(username);

        // Se eliminaron los System.out de depuración: imprimían si la
        // contraseña era correcta y el rol real del usuario, información
        // útil para un atacante que lea los registros del servidor.
        if (usuario == null) {
            return false;
        }

        // Verifica: contraseña, rol y que el usuario esté activo
        boolean passwordCorrecta =
            passwordEncoder.matches(password, usuario.getPasswordHash());
        boolean valido =
            passwordCorrecta &&
            usuario.getRol().equalsIgnoreCase(rol) &&
            usuario.isActivo();

        if (valido) {
            fortalecerHashSiHaceFalta(usuario, password);

            // Auditoría: registra evento de inicio de sesión
            auditor.accept(
                new EventoAuditoria(usuario.getId(), "INICIO_SESION")
            );
        }

        return valido;
    }

    /**
     * Actualización de hash en el inicio de sesión.
     * PROBLEMA: algunos usuarios existentes tienen hashes con costo 6 (muy
     *           débil). No se pueden recalcular sin conocer su contraseña.
     * SOLUCIÓN: cuando el usuario inicia sesión correctamente, ya tenemos su
     *           contraseña en claro un instante, así que se vuelve a
     *           guardar con el costo actual. Los hashes ya fuertes no se
     *           tocan.
     * Si falla la actualización, el login NO se cancela: es una mejora, no
     * un requisito para entrar.
     */
    private void fortalecerHashSiHaceFalta(Usuario usuario, String password) {
        if (passwordEncoder.upgradeEncoding(usuario.getPasswordHash())) {
            try {
                usuarioDAO.actualizarPasswordHash(
                    (long) usuario.getId(), passwordEncoder.encode(password));
            } catch (RuntimeException e) {
                // se intentará de nuevo en el próximo inicio de sesión
            }
        }
    }

    // Crear nuevo usuario
    public Usuario crearUsuario(String username, String password, String rol) {
        Usuario usuario = new Usuario();

        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario creado = usuarioDAO.crear(usuario);

        if (creado != null) {
            // Auditoría: registra evento de creación de usuario
            auditor.accept(
                new EventoAuditoria(creado.getId(), "CREO_USUARIO")
            );
        }

        return creado;
    }

    // Cambiar estado de usuario (activar/desactivar)
    public boolean cambiarEstado(Long id, boolean activo) {
        // Actualiza estado en BD según ID
        return usuarioDAO.cambiarEstado(id, activo);
    }

}
