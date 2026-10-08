package com.proyecto.dao;

import com.proyecto.model.Bitacora;
import com.proyecto.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class BitacoraDAO implements IBitacoraDAO {

    @Override
    public List<Bitacora> listar(
            Integer usuarioId,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) throws SQLException {

        Connection con = ConexionBD.getInstancia().getConexion();

        StringBuilder sql = new StringBuilder("""
            SELECT
                b.id,
                b.usuario_id,
                u.username,
                u.rol,
                b.accion,
                b.fecha
            FROM bitacora_acceso b
            INNER JOIN usuario u
                ON b.usuario_id = u.id
            WHERE 1 = 1
            """);

        List<Object> parametros = new ArrayList<>();

        if (usuarioId != null) {
            sql.append(" AND b.usuario_id = ?");
            parametros.add(usuarioId);
        }

        if (fechaDesde != null) {
            sql.append(" AND b.fecha >= ?");
            parametros.add(java.sql.Timestamp.valueOf(
                    fechaDesde.atStartOfDay()
            ));
        }

        if (fechaHasta != null) {
            sql.append(" AND b.fecha < ?");
            parametros.add(java.sql.Timestamp.valueOf(
                    fechaHasta.plusDays(1).atStartOfDay()
            ));
        }

        sql.append(" ORDER BY b.fecha DESC");

        List<Bitacora> lista = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Bitacora bitacora = new Bitacora();

                    bitacora.setId(rs.getInt("id"));
                    bitacora.setUsuarioId(rs.getInt("usuario_id"));
                    bitacora.setUsername(rs.getString("username"));
                    bitacora.setRol(rs.getString("rol"));
                    bitacora.setAccion(rs.getString("accion"));

                    if (rs.getTimestamp("fecha") != null) {
                        bitacora.setFecha(
                            rs.getTimestamp("fecha").toLocalDateTime()
                        );
                    }

                    lista.add(bitacora);
                }
            }
        }

        return lista;
    }

    @Override
    public void registrar(
            int usuarioId,
            String accion
    ) throws SQLException {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = """
            INSERT INTO bitacora_acceso
                (usuario_id, accion)
            VALUES (?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, accion);

            ps.executeUpdate();
        }
    }
}