package com.proyecto.dao.factory;

import com.proyecto.model.Cuenta;
import com.proyecto.model.CuentaAhorro;
import com.proyecto.model.CuentaCorriente;
import java.util.Map;
import java.util.function.Supplier;

/**
 * PATRÓN: Factory (Creacional), variante "fábrica simple".
 *
 * PROBLEMA: CuentaDAO decidía el tipo de cuenta con un if/ternario
 *           ("CORRIENTE" ? new CuentaCorriente() : new CuentaAhorro()).
 *           Agregar un tipo nuevo (por ejemplo CuentaSueldo) obligaba a
 *           modificar el DAO, rompiendo el principio O (Abierto/Cerrado).
 * SOLUCIÓN: la creación se mueve aquí. Cada tipo se registra una vez en la
 *           tabla TIPOS; el DAO solo pide crear(tipo).
 * POR QUÉ NO OTRO: Prototype copiaría un objeto existente, pero aquí cada
 *           cuenta se construye desde una fila de la base de datos, no se
 *           clona. Abstract Factory sobra: solo hay una familia de
 *           productos (Cuenta).
 * SIN EL PATRÓN: cada tipo nuevo exigiría editar el if en el DAO y
 *           arriesgar romper lo que ya funciona.
 *
 * Comportamiento conservado: un tipo desconocido se trata como AHORRO, igual
 * que antes.
 */
public class CuentaFactory {

    // Para agregar un tipo de cuenta, solo se añade una línea aquí
    private static final Map<String, Supplier<Cuenta>> TIPOS = Map.of(
        "AHORRO", CuentaAhorro::new,
        "CORRIENTE", CuentaCorriente::new
    );

    private CuentaFactory() { }

    public static Cuenta crear(String tipo) {
        String clave = tipo == null ? "" : tipo.trim().toUpperCase();
        return TIPOS.getOrDefault(clave, CuentaAhorro::new).get();
    }
}
