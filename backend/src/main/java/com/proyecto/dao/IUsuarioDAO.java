package com.proyecto.dao;

import com.proyecto.model.Usuario;
import java.util.List;

/**
 * Contrato de acceso a datos de la entidad Usuario.
 *
 * PRINCIPIO: D de SOLID (Inversión de Dependencias).
 * PROBLEMA: UsuarioService y la DAOFactory dependían de la clase concreta
 *           UsuarioDAO (JDBC). Esta interfaz además estaba desactualizada:
 *           sus firmas no coincidían con las de UsuarioDAO, que ni siquiera
 *           la implementaba.
 * SOLUCIÓN: la interfaz refleja las operaciones reales y UsuarioDAO la
 *           implementa. Los servicios dependen de este contrato, no de JDBC.
 * BENEFICIO: se puede cambiar o simular la persistencia sin tocar el
 *            servicio.
 */
public interface IUsuarioDAO {
    Usuario buscarPorUsername(String username);
    Usuario buscarPorId(Long id);
    boolean cambiarRol(Long id, String rol);
    List<Usuario> listarTodos();
    Usuario crear(Usuario usuario);
    boolean cambiarEstado(Long id, boolean activo);
    boolean actualizarPasswordHash(Long id, String nuevoHash);
}
