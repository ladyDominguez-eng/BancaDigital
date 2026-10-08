package com.proyecto.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.dao.UsuarioDAO;
import com.proyecto.model.Usuario;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioDAO usuarioDAO;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Método para validar usuario
    public boolean validarUsuario(String username, String password, String rol) {

    Usuario usuario = usuarioDAO.buscarPorUsername(username);

    System.out.println("👤 Usuario encontrado: " + (usuario != null));

    if (usuario == null) {
        return false;
    }

    System.out.println("🎭 Rol BD: " + usuario.getRol());
    System.out.println("🎭 Rol recibido: " + rol);
    System.out.println("🟢 Activo: " + usuario.isActivo());
    System.out.println("🔐 Password correcta: " +
            passwordEncoder.matches(password, usuario.getPasswordHash()));

    boolean valido =
        passwordEncoder.matches(password, usuario.getPasswordHash()) &&
        usuario.getRol().equalsIgnoreCase(rol) &&
        usuario.isActivo();

if (valido) {
    AuditoriaPublisher.getInstancia().publicar(
        new EventoAuditoria(
            usuario.getId(),
            "INICIO_SESION"
        )
    );
}

return valido;
}
    public Usuario crearUsuario(String username, String password, String rol) {
    Usuario usuario = new Usuario();

    usuario.setUsername(username);
    usuario.setPasswordHash(passwordEncoder.encode(password));
    usuario.setRol(rol);
    usuario.setActivo(true);

    Usuario creado = usuarioDAO.crear(usuario);

if (creado != null) {
    AuditoriaPublisher.getInstancia().publicar(
        new EventoAuditoria(
            creado.getId(),
            "CREO_USUARIO"
        )
    );
}

return creado;
}
public boolean cambiarEstado(Long id, boolean activo) {

    return usuarioDAO.cambiarEstado(id, activo);
}

}
