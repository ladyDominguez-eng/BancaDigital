package com.proyecto.dao;

import com.proyecto.model.InformesUsuarios;
import com.proyecto.model.InformesCuentas;
import com.proyecto.model.InformesTransacciones;
import com.proyecto.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InformesDAO {

    public List<InformesUsuarios> usuariosPorRol() throws SQLException {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = """
            SELECT
                rol,
                COUNT(*) AS total,
                SUM(CASE WHEN activo = true THEN 1 ELSE 0 END) AS activos
            FROM usuario
            GROUP BY rol
            ORDER BY rol
            """;

        List<InformesUsuarios> lista = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                InformesUsuarios informe = new InformesUsuarios();

                informe.setRol(rs.getString("rol"));
                informe.setTotal(rs.getInt("total"));
                informe.setActivos(rs.getInt("activos"));

                lista.add(informe);
            }
        }

        return lista;
    }


    public InformesCuentas resumenCuentas() throws SQLException {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = """
            SELECT
                (SELECT COUNT(*) FROM cliente) AS total_clientes,
                (SELECT COUNT(*) FROM cuenta) AS total_cuentas,
                COALESCE((SELECT SUM(saldo) FROM cuenta), 0) AS saldo_total
            """;

        InformesCuentas informe = new InformesCuentas();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                informe.setTotalClientes(
                    rs.getInt("total_clientes")
                );

                informe.setTotalCuentas(
                    rs.getInt("total_cuentas")
                );

                informe.setSaldoTotal(
                    rs.getBigDecimal("saldo_total")
                );
            }
        }

        return informe;
    }


    public List<InformesTransacciones> transaccionesPorTipo()
            throws SQLException {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = """
            SELECT
                tipo,
                COUNT(*) AS cantidad,
                COALESCE(SUM(monto), 0) AS monto_total
            FROM transaccion
            GROUP BY tipo
            ORDER BY tipo
            """;

        List<InformesTransacciones> lista = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                InformesTransacciones informe =
                    new InformesTransacciones();

                informe.setTipo(rs.getString("tipo"));
                informe.setCantidad(rs.getInt("cantidad"));
                informe.setMontoTotal(
                    rs.getBigDecimal("monto_total")
                );

                lista.add(informe);
            }
        }

        return lista;
    }
}