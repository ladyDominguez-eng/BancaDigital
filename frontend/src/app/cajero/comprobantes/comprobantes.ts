import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajeroService } from '../../services/cajero';
import { Transaccion } from '../../services/cliente';

/** RF-14: el cajero consulta e imprime el comprobante de una operación. */
@Component({
  selector: 'app-comprobantes',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './comprobantes.html'
})
export class Comprobantes {
  private servicio = inject(CajeroService);

  codigo = '';
  comprobante = signal<Transaccion | null>(null);
  mensaje = signal('');

  buscar() {
    this.comprobante.set(null);
    if (!this.codigo.trim()) {
      this.mensaje.set('Ingresa el código del comprobante.');
      return;
    }
    this.servicio.comprobante(this.codigo.trim()).subscribe({
      next: t => { this.comprobante.set(t); this.mensaje.set(''); },
      error: e => this.mensaje.set(e.error?.error ?? 'No se pudo consultar el comprobante.')
    });
  }

  imprimir() { window.print(); }
}
