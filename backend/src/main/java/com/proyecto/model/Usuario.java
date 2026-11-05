package com.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Usuario {
    private int id;
    private String username;
    private String passwordHash;
    private String rol;
    private boolean activo;

    public int getId() { return id; }
    public void setId(long id) { this.id = (int) id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /**
     * SEGURIDAD: @JsonIgnore evita que el hash de la contraseña salga en el
     * JSON. PROBLEMA: GET /api/usuarios (sin autenticación) y la respuesta
     * de crear usuario devolvían el hash de cada usuario, lo que permite
     * ataques de fuerza bruta fuera de línea. El hash sigue disponible
     * dentro del servidor (el login lo necesita), pero nunca se serializa.
     */
    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
