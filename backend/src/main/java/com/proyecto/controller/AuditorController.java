package com.proyecto.controller;

import com.proyecto.model.ArchivoExportado;
import com.proyecto.seguridad.AuthInterceptor;
import com.proyecto.seguridad.Sesion;
import com.proyecto.service.AuditorService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GRASP Controlador del rol AUDITOR. Solo recibe y delega. Acceso permitido
 * solo al rol AUDITOR (ReglasAcceso). Las fechas las convierte Spring
 * (formato AAAA-MM-DD), así no se repite el código de conversión.
 */
@RestController
@RequestMapping("/api/auditor")
public class AuditorController {

    private final AuditorService service;

    public AuditorController(AuditorService service) {
        this.service = service;
    }

    @GetMapping("/transacciones")
    public ResponseEntity<?> transacciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) BigDecimal montoMinimo) throws SQLException {
        return ResponseEntity.ok(service.revisar(tipo, desde, hasta, montoMinimo));
    }

    @GetMapping("/riesgo")
    public ResponseEntity<?> riesgo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta)
            throws SQLException {
        return ResponseEntity.ok(service.reporteRiesgo(desde, hasta));
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_SESION) Sesion sesion,
            @RequestParam(defaultValue = "csv") String formato,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) BigDecimal montoMinimo) throws SQLException {

        ArchivoExportado archivo = service.exportar(
                sesion.usuarioId(), formato, tipo, desde, hasta, montoMinimo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.nombre() + "\"")
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .body(archivo.contenido());
    }
}
