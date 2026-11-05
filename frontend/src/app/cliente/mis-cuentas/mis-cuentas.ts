import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ClienteService, CuentaCliente, Transaccion } from '../../services/cliente';

/** RF-04: el cliente consulta el saldo y los movimientos de SUS cuentas. */
@Component({
  selector: 'app-mis-cuentas',
  imports: [DecimalPipe, DatePipe],
  templateUrl: './mis-cuentas.html'
})
export class MisCuentas implements OnInit {
  private servicio = inject(ClienteService);

  cuentas = signal<CuentaCliente[]>([]);
  seleccionada = signal<CuentaCliente | null>(null);
  movimientos = signal<Transaccion[]>([]);
  mensaje = signal('');

  ngOnInit() {
    this.servicio.cuentas().subscribe({
      next: lista => {
        this.cuentas.set(lista);
        if (lista.length) this.seleccionar(lista[0]);
      },
      error: e => this.mensaje.set(e.error?.error ?? 'No se pudieron cargar tus cuentas.')
    });
  }

  seleccionar(c: CuentaCliente) {
    this.seleccionada.set(c);
    this.servicio.movimientos(c.id).subscribe({
      next: lista => this.movimientos.set(lista),
      error: e => this.mensaje.set(e.error?.error ?? 'No se pudieron cargar los movimientos.')
    });
  }

  // Un movimiento resta si el dinero salió de la cuenta seleccionada
  esSalida(m: Transaccion): boolean {
    return m.cuentaOrigen === this.seleccionada()?.numero;
  }
}
