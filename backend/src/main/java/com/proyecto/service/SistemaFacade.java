package com.proyecto.service;

import com.proyecto.model.Cuenta;
import com.proyecto.model.PagoRequest;
import com.proyecto.model.SolicitudCredito;
import com.proyecto.model.SolicitudCreditoRequest;
import com.proyecto.model.TransaccionVista;
import com.proyecto.model.TransferenciaRequest;
import com.proyecto.patterns.AuditoriaPublisher;
import com.proyecto.patterns.EventoAuditoria;
import com.proyecto.seguridad.Sesion;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * PATRÓN: Facade (Estructural).
 *
 * PROBLEMA: una operación del cliente combina varios servicios: consultar
 *           sus cuentas (ClienteService), validar y mover el dinero
 *           (TransferenciaService) y dejar constancia en la bitácora
 *           (Observer de auditoría). Si el controlador llamara a cada uno,
 *           conocería todos los detalles y repetiría esa secuencia en cada
 *           endpoint; si mañana se agrega un paso (por ejemplo notificar al
 *           cliente), habría que cambiar todos los controladores.
 * SOLUCIÓN: esta clase ofrece métodos simples (transferir, pagarServicio...)
 *           y esconde la coordinación. El controlador solo conoce la fachada.
 * POR QUÉ NO OTRO: Mediator sirve cuando muchos objetos se comunican entre
 *           sí; aquí el flujo es lineal y siempre empieza en el controlador,
 *           por eso basta simplificar la entrada. Decorator añadiría
 *           comportamiento a UN servicio, no coordina varios.
 * SIN EL PATRÓN: el controlador mezclaría validación, dinero y auditoría
 *           (objeto todopoderoso) y cada cambio tocaría muchos archivos.
 *
 * GRASP: el controlador queda solo como "Controlador" (recibe y responde).
 */
@Service
public class SistemaFacade {

    private final ClienteService clienteService;
    private final TransferenciaService transferenciaService;
    private final AuditoriaPublisher auditoria;

    private final CreditoService creditoService;

    public SistemaFacade(ClienteService clienteService,
            TransferenciaService transferenciaService,
            CreditoService creditoService,
            AuditoriaPublisher auditoria) {
        this.clienteService = clienteService;
        this.transferenciaService = transferenciaService;
        this.creditoService = creditoService;
        this.auditoria = auditoria;
    }

    // Crédito: el cliente solicita y consulta; el asesor las gestiona por otro controlador
    public SolicitudCredito solicitarCredito(Sesion sesion, SolicitudCreditoRequest r) throws SQLException {
        return creditoService.solicitar(sesion.usuarioId(), r.monto(), r.plazoMeses());
    }

    public List<SolicitudCredito> misSolicitudes(Sesion sesion) throws SQLException {
        return creditoService.misSolicitudes(sesion.usuarioId());
    }

    public List<Cuenta> misCuentas(Sesion sesion) throws SQLException {
        return clienteService.misCuentas(sesion.usuarioId());
    }

    public List<TransaccionVista> movimientos(Sesion sesion, int cuentaId) throws SQLException {
        return clienteService.movimientos(sesion.usuarioId(), cuentaId);
    }

    public TransaccionVista transferir(Sesion sesion, TransferenciaRequest r) throws SQLException {
        TransaccionVista t = transferenciaService.transferir(
                sesion.usuarioId(), r.cuentaOrigenId(), r.numeroDestino(), r.monto());
        // Observer: la bitácora se entera sin que este código conozca la base de datos
        auditoria.publicar(new EventoAuditoria(sesion.usuarioId(), "TRANSFERENCIA " + t.comprobante()));
        return t;
    }

    public TransaccionVista pagarServicio(Sesion sesion, PagoRequest r) throws SQLException {
        TransaccionVista t = transferenciaService.pagarServicio(
                sesion.usuarioId(), r.cuentaId(), r.servicio(), r.monto());
        auditoria.publicar(new EventoAuditoria(sesion.usuarioId(), "PAGO_SERVICIO " + t.comprobante()));
        return t;
    }
}
