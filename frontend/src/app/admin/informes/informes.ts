import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { InformesService } from '../../services/informes';

@Component({
  selector: 'app-informes',
  imports: [DecimalPipe],
  templateUrl: './informes.html',
  styleUrl: './informes.css'
})
export class Informes implements OnInit {

  private servicio = inject(InformesService);

  usuarios = signal<any[]>([]);
  cuentas = signal<any>({});
  transacciones = signal<any[]>([]);

  ngOnInit() {
  this.cargarInformes();
}

  cargarInformes() {

    this.servicio.usuariosPorRol().subscribe({
      next: datos => {
        console.log('USUARIOS RECIBIDOS:', datos);
        this.usuarios.set(datos);
      },
      error: e => console.error('ERROR usuarios:', e)
    });

    this.servicio.resumenCuentas().subscribe({
      next: datos => {
        console.log('CUENTAS RECIBIDAS:', datos);
        this.cuentas.set(datos);
      },
      error: e => console.error('ERROR cuentas:', e)
    });

    this.servicio.transaccionesPorTipo().subscribe({
      next: datos => {
        console.log('TRANSACCIONES RECIBIDAS:', datos);
        this.transacciones.set(datos);
      },
      error: e => console.error('ERROR transacciones:', e)
    });

  }
}