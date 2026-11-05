import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService, CuentaCliente, Servicio, Transaccion } from '../../services/cliente';

/** Pago de servicios (luz, agua, internet, teléfono) desde una cuenta propia. */
@Component({
  selector: 'app-pagos',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './pagos.html'
})
export class Pagos implements OnInit {
  private servicioCliente = inject(ClienteService);

  cuentas = signal<CuentaCliente[]>([]);
  servicios = signal<Servicio[]>([]);
  comprobante = signal<Transaccion | null>(null);
  mensaje = signal('');
  enviando = signal(false);

  cuentaId: number | null = null;
  servicio = '';
  monto: number | null = null;

  ngOnInit() {
    this.cargarCuentas();
    this.servicioCliente.servicios().subscribe({
      next: lista => {
        this.servicios.set(lista);
        if (lista.length) this.servicio = lista[0].codigo;
      },
      error: e => this.mensaje.set(e.error?.error ?? 'No se pudo cargar la lista de servicios.')
    });
  }

  private cargarCuentas() {
    this.servicioCliente.cuentas().subscribe({
      next: lista => {
        this.cuentas.set(lista);
        if (this.cuentaId == null && lista.length) this.cuentaId = lista[0].id;
      },
      error: e => this.mensaje.set(e.error?.error ?? 'No se pudieron cargar tus cuentas.')
    });
  }

  pagar() {
    this.comprobante.set(null);
    if (this.cuentaId == null || !this.servicio || !this.monto) {
      this.mensaje.set('Elige la cuenta, el servicio y el monto.');
      return;
    }
    this.enviando.set(true);
    this.servicioCliente.pagar(this.cuentaId, this.servicio, this.monto).subscribe({
      next: t => {
        this.enviando.set(false);
        this.mensaje.set('');
        this.comprobante.set(t);
        this.monto = null;
        this.cargarCuentas();
      },
      error: e => {
        this.enviando.set(false);
        this.mensaje.set(e.error?.error ?? 'No se pudo realizar el pago.');
      }
    });
  }
}
