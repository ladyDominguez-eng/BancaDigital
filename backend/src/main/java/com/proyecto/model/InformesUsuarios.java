package com.proyecto.model;

public class InformesUsuarios {

    private String rol;
    private int total;
    private int activos;

    public InformesUsuarios() {
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getActivos() {
        return activos;
    }

    public void setActivos(int activos) {
        this.activos = activos;
    }
}