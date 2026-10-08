package com.proyecto.controller;

import com.proyecto.model.Movimiento;
import com.proyecto.model.ResultadoOperacionCajero;
import com.proyecto.service.CajeroService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cajero")
@CrossOrigin(origins = "http://localhost:4200")
public class CajeroController {

    private final CajeroService cajeroService;

    public CajeroController(CajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @PostMapping("/depositos")
    public ResultadoOperacionCajero registrarDeposito(
            @RequestBody Movimiento movimiento) {

        return cajeroService.registrarDeposito(movimiento);
    }

    @PostMapping("/retiros")
    public ResultadoOperacionCajero registrarRetiro(
            @RequestBody Movimiento movimiento) {

        return cajeroService.registrarRetiro(movimiento);
    }
}
