package com.proyecto.controller;

import com.proyecto.model.PagoRequest;
import com.proyecto.model.ServicioPago;
import com.proyecto.model.SolicitudCreditoRequest;
import com.proyecto.model.TransferenciaRequest;
import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.Sesion;
import com.proyecto.service.SistemaFacade;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador del rol CLIENTE. Solo recibe la petición y delega en la
 * fachada (SistemaFacade). Identifica al cliente por la sesión del token,
 * nunca por un id enviado en la petición: así nadie puede pedir las cuentas
 * de otra persona. Acceso permitido solo al rol CLIENTE (ReglasAcceso).
 */
@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final SistemaFacade fachada;

    public ClienteController(SistemaFacade fachada) {
        this.fachada = fachada;
    }

    @GetMapping("/cuentas")
    public ResponseEntity<?> cuentas(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion) throws SQLException {
        return ResponseEntity.ok(fachada.misCuentas(sesion));
    }

    @GetMapping("/cuentas/{id}/movimientos")
    public ResponseEntity<?> movimientos(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @PathVariable int id) throws SQLException {
        return ResponseEntity.ok(fachada.movimientos(sesion, id));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<?> transferir(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @RequestBody TransferenciaRequest request) throws SQLException {
        return ResponseEntity.ok(fachada.transferir(sesion, request));
    }

    // Lista de servicios que se pueden pagar (para el desplegable del frontend)
    @GetMapping("/servicios")
    public ResponseEntity<?> servicios() {
        List<Map<String, String>> lista = Arrays.stream(ServicioPago.values())
                .map(s -> Map.of("codigo", s.name(), "nombre", s.getNombre()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/creditos")
    public ResponseEntity<?> misCreditos(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion) throws SQLException {
        return ResponseEntity.ok(fachada.misSolicitudes(sesion));
    }

    @PostMapping("/creditos")
    public ResponseEntity<?> solicitarCredito(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @RequestBody SolicitudCreditoRequest request) throws SQLException {
        return ResponseEntity.ok(fachada.solicitarCredito(sesion, request));
    }

    @PostMapping("/pagos")
    public ResponseEntity<?> pagar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @RequestBody PagoRequest request) throws SQLException {
        return ResponseEntity.ok(fachada.pagarServicio(sesion, request));
    }
}
