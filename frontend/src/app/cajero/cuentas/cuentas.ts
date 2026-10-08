import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { CajeroService, CuentaDto } from '../../services/cajero';

@Component({
  selector: 'app-cuentas',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './cuentas.html',
  styleUrl: './cuentas.css'
})
export class Cuentas {
  private servicio = inject(CajeroService);

  dni = '';
  numero = '';
  cuentas = signal<CuentaDto[]>([]);
  mensaje = signal('');
  esError = signal(false);

  buscar() {
    this.servicio.consultar(this.dni.trim(), this.numero.trim()).subscribe({
      next: lista => {
        this.cuentas.set(lista);
        this.avisar(lista.length ? '' : 'No se encontraron cuentas.', lista.length === 0);
      },
      error: e => {
        this.cuentas.set([]);
        this.avisar(e.error?.error ?? 'No se pudo consultar.', true);
      }
    });
  }

  limpiar() {
    this.dni = '';
    this.numero = '';
    this.cuentas.set([]);
    this.avisar('', false);
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}