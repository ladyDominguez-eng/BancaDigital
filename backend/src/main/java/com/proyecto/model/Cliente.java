package com.proyecto.model;

/** Cliente de la financiera: persona asociada a un usuario con rol CLIENTE. */
public record Cliente(int id, int usuarioId, String dni, String nombres, String apellidos) {

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
