package com.proyecto.controller;

import com.proyecto.model.ObservacionRequest;
import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.Sesion;
import com.proyecto.service.CreditoService;
import java.sql.SQLException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador del rol ASESOR (créditos). Solo recibe y delega: las
 * acciones permitidas en cada estado las decide el patrón State dentro de
 * la solicitud. Acceso permitido solo al rol ASESOR (ReglasAcceso).
 */
@RestController
@RequestMapping("/api/asesor")
public class AsesorController {

    private final CreditoService creditos;

    public AsesorController(CreditoService creditos) {
        this.creditos = creditos;
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<?> listar(@RequestParam(required = false) String estado) throws SQLException {
        return ResponseEntity.ok(creditos.listar(estado));
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<?> detalle(@PathVariable int id) throws SQLException {
        return ResponseEntity.ok(creditos.detalle(id));
    }

    @PostMapping("/solicitudes/{id}/evaluar")
    public ResponseEntity<?> evaluar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @PathVariable int id) throws SQLException {
        return ResponseEntity.ok(creditos.evaluar(sesion.usuarioId(), id));
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    public ResponseEntity<?> aprobar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @PathVariable int id,
            @RequestBody(required = false) ObservacionRequest body) throws SQLException {
        return ResponseEntity.ok(creditos.aprobar(sesion.usuarioId(), id, texto(body)));
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public ResponseEntity<?> rechazar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @PathVariable int id,
            @RequestBody(required = false) ObservacionRequest body) throws SQLException {
        return ResponseEntity.ok(creditos.rechazar(sesion.usuarioId(), id, texto(body)));
    }

    @PostMapping("/solicitudes/{id}/observaciones")
    public ResponseEntity<?> observar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @PathVariable int id,
            @RequestBody ObservacionRequest body) throws SQLException {
        return ResponseEntity.ok(creditos.registrarObservacion(sesion.usuarioId(), id, body.observaciones()));
    }

    @GetMapping("/clientes/{clienteId}/historial")
    public ResponseEntity<?> historial(@PathVariable int clienteId) throws SQLException {
        return ResponseEntity.ok(creditos.historial(clienteId));
    }

    private String texto(ObservacionRequest body) {
        return body == null ? null : body.observaciones();
    }
}
