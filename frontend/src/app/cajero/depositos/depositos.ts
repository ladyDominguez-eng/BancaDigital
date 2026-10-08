import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CajeroService } from '../../services/cajero';

@Component({
  selector: 'app-depositos',
  imports: [FormsModule],
  templateUrl: './depositos.html',
  styleUrl: './depositos.css'
})
export class Depositos {
  private servicio = inject(CajeroService);

  numeroCuenta = '';
  monto: number | null = null;
  mensaje = signal('');
  esError = signal(false);

  depositar() {
    if (!this.numeroCuenta.trim() || !this.monto) {
      this.avisar('Completa el número de cuenta y el monto.', true);
      return;
    }
    this.servicio.depositar(this.numeroCuenta.trim(), this.monto).subscribe({
      next: r => {
        this.avisar(`${r.mensaje} Operación N° ${r.transaccionId}.`, false);
        this.numeroCuenta = '';
        this.monto = null;
      },
      error: e => this.avisar(e.error?.error ?? 'No se pudo registrar el depósito.', true)
    });
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}