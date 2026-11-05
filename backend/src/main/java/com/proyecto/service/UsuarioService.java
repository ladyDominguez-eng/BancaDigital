package com.proyecto.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;
import com.proyecto.dao.IUsuarioDAO;
import com.proyecto.model.Usuario;
import com.proyecto.util.Validaciones;
import java.util.List;
import java.util.Set;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;

@Service
public class UsuarioService {

    // Costo de BCrypt. Antes se usaba el valor por defecto sin dejarlo
    // explícito; ahora es una constante con nombre (antipatrón "valores
    // mágicos"). 10 es el mínimo recomendado hoy.
    // (sin "private": RegistroClienteService usa el mismo costo)
    static final int COSTO_BCRYPT = 10;

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

    // Valida las credenciales y dice solo si son correctas o no
    public boolean validarUsuario(String username, String password, String rol) {
        return autenticar(username, password, rol) != null;
    }

    /**
     * Valida las credenciales y devuelve el Usuario autenticado, o null si
     * no son correctas. El login lo necesita para crear la sesión (token)
     * con el id y el rol reales guardados en la base de datos.
     */
    public Usuario autenticar(String username, String password, String rol) {

        Usuario usuario = usuarioDAO.buscarPorUsername(username);

        // Se eliminaron los System.out de depuración: imprimían si la
        // contraseña era correcta y el rol real del usuario, información
        // útil para un atacante que lea los registros del servidor.
        if (usuario == null) {
            return null;
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

        return valido ? usuario : null;
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

    // Roles que existen en el sistema
    static final Set<String> ROLES = Set.of("ADMIN", "ASESOR", "CAJERO", "AUDITOR", "CLIENTE");

    /**
     * Crear un usuario del personal (Administrador, Asesor, Cajero, Auditor).
     *
     * PROBLEMA: antes se aceptaba cualquier texto como rol, usuario o
     *           contraseña (incluso vacíos), un usuario repetido terminaba
     *           en error 500 y se podía crear un usuario CLIENTE sin
     *           cliente ni cuenta, que entraba al sistema y no veía nada.
     * SOLUCIÓN: se valida todo aquí (GRASP Experto: el servicio conoce las
     *           reglas) y los clientes se dan de alta con RegistroClienteService,
     *           que crea usuario, cliente y cuenta juntos.
     *
     * @param actorId administrador que realiza la acción (para la auditoría)
     */
    public Usuario crearUsuario(int actorId, String username, String password, String rol) {
        Validaciones.usernameValido(username);
        Validaciones.claveValida(password);
        String rolNormalizado = rolValido(rol);
        if ("CLIENTE".equals(rolNormalizado)) {
            throw new IllegalArgumentException(
                "Los clientes se registran con la opción \"Registrar cliente\", "
                + "que también crea su cuenta.");
        }
        if (usuarioDAO.buscarPorUsername(username) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rolNormalizado);
        usuario.setActivo(true);

        Usuario creado = usuarioDAO.crear(usuario);

        if (creado != null) {
            // Auditoría: ahora se guarda QUIÉN creó el usuario (antes se
            // guardaba el id del usuario nuevo, que no decía nada del autor)
            auditor.accept(
                new EventoAuditoria(actorId, "CREO_USUARIO " + creado.getUsername())
            );
        }

        return creado;
    }

    /**
     * Cambiar el rol de un usuario del personal.
     *
     * Reglas (cada una evita un estado inconsistente):
     * - Nadie cambia su propio rol: un administrador podría quedarse sin
     *   acceso y el sistema sin administradores.
     * - No se pasa a ni desde CLIENTE: un cliente es además una fila en
     *   "cliente" con cuentas; cambiarle el rol dejaría datos huérfanos.
     */
    public Usuario cambiarRol(int actorId, Long id, String rol) {
        String nuevoRol = rolValido(rol);
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        if (usuario.getId() == actorId) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
        }
        if ("CLIENTE".equals(usuario.getRol()) || "CLIENTE".equals(nuevoRol)) {
            throw new IllegalArgumentException(
                "El rol CLIENTE no se puede asignar ni quitar: el cliente tiene cuentas asociadas.");
        }
        usuarioDAO.cambiarRol(id, nuevoRol);
        usuario.setRol(nuevoRol);
        auditor.accept(new EventoAuditoria(actorId,
                "CAMBIO_ROL " + usuario.getUsername() + " -> " + nuevoRol));
        return usuario;
    }

    // Cambiar estado de usuario (activar/desactivar)
    public boolean cambiarEstado(Long id, boolean activo) {
        // Actualiza estado en BD según ID
        return usuarioDAO.cambiarEstado(id, activo);
    }

    private String rolValido(String rol) {
        String normalizado = rol == null ? "" : rol.trim().toUpperCase();
        if (!ROLES.contains(normalizado)) {
            throw new IllegalArgumentException("El rol no es válido.");
        }
        return normalizado;
    }

}
