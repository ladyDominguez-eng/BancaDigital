package com.proyecto.controller;

import com.proyecto.model.DepositoRequest;
import com.proyecto.model.TransaccionVista;
import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.Sesion;
import com.proyecto.service.CajeroService;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador del rol CAJERO: recibe la petición y delega en el
 * Service. Los errores y el CORS ya no se repiten aquí: los atienden
 * ManejadorErrores y CorsConfig. El rol lo exige ReglasAcceso.
 */
@RestController
@RequestMapping("/api/cajero")
public class CajeroController {

    private final CajeroService service;

    public CajeroController(CajeroService service) {
        this.service = service;
    }

    @GetMapping("/cuentas")
    public ResponseEntity<?> consultar(@RequestParam(required = false) String dni,
                                       @RequestParam(required = false) String numero)
            throws SQLException {
        return ResponseEntity.ok(service.consultar(dni, numero));
    }

    @PostMapping("/depositos")
    public ResponseEntity<?> depositar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @RequestBody DepositoRequest req) throws SQLException {
        TransaccionVista t = service.depositar(sesion.usuarioId(), req.getNumeroCuenta(), req.getMonto());
        return ResponseEntity.ok(respuesta("Depósito registrado.", t));
    }

    // Un retiro recibe los mismos datos que un depósito (cuenta y monto)
    @PostMapping("/retiros")
    public ResponseEntity<?> retirar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @RequestBody DepositoRequest req) throws SQLException {
        TransaccionVista t = service.retirar(sesion.usuarioId(), req.getNumeroCuenta(), req.getMonto());
        return ResponseEntity.ok(respuesta("Retiro registrado.", t));
    }

    // Consulta un comprobante emitido (RF-14)
    @GetMapping("/comprobantes/{codigo}")
    public ResponseEntity<?> comprobante(@PathVariable String codigo) throws SQLException {
        return ResponseEntity.ok(service.buscarComprobante(codigo));
    }

    // transaccionId se mantiene para no romper el frontend existente
    private Map<String, Object> respuesta(String mensaje, TransaccionVista t) {
        return Map.of("mensaje", mensaje, "transaccionId", t.id(), "comprobante", t.comprobante());
    }
}
