import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService, CuentaCliente, Transaccion } from '../../services/cliente';

/** RF-05: transferencia entre cuentas. Las reglas las valida el backend (Facade). */
@Component({
  selector: 'app-transferir',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './transferir.html'
})
export class Transferir implements OnInit {
  private servicio = inject(ClienteService);

  cuentas = signal<CuentaCliente[]>([]);
  comprobante = signal<Transaccion | null>(null);
  mensaje = signal('');
  enviando = signal(false);

  cuentaOrigenId: number | null = null;
  numeroDestino = '';
  monto: number | null = null;

  ngOnInit() { this.cargarCuentas(); }

  private cargarCuentas() {
    this.servicio.cuentas().subscribe({
      next: lista => {
        this.cuentas.set(lista);
        if (this.cuentaOrigenId == null && lista.length) this.cuentaOrigenId = lista[0].id;
      },
      error: e => this.mensaje.set(e.error?.error ?? 'No se pudieron cargar tus cuentas.')
    });
  }

  transferir() {
    this.comprobante.set(null);
    if (this.cuentaOrigenId == null || !this.numeroDestino.trim() || !this.monto) {
      this.mensaje.set('Completa la cuenta de origen, la cuenta de destino y el monto.');
      return;
    }
    this.enviando.set(true);
    this.servicio.transferir(this.cuentaOrigenId, this.numeroDestino.trim(), this.monto).subscribe({
      next: t => {
        this.enviando.set(false);
        this.mensaje.set('');
        this.comprobante.set(t);
        this.numeroDestino = '';
        this.monto = null;
        this.cargarCuentas(); // refresca los saldos
      },
      error: e => {
        this.enviando.set(false);
        this.mensaje.set(e.error?.error ?? 'No se pudo realizar la transferencia.');
      }
    });
  }
}
