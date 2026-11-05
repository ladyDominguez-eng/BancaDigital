import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService, Solicitud } from '../../services/cliente';

/** RF-06: el cliente solicita un crédito y sigue el estado de sus solicitudes. */
@Component({
  selector: 'app-creditos',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './creditos.html'
})
export class Creditos implements OnInit {
  private servicio = inject(ClienteService);

  solicitudes = signal<Solicitud[]>([]);
  mensaje = signal('');
  esError = signal(false);
  enviando = signal(false);

  monto: number | null = null;
  plazoMeses: number | null = 12;

  ngOnInit() { this.cargar(); }

  private cargar() {
    this.servicio.creditos().subscribe({
      next: lista => this.solicitudes.set(lista),
      error: e => this.avisar(e.error?.error ?? 'No se pudieron cargar tus solicitudes.', true)
    });
  }

  solicitar() {
    if (!this.monto || !this.plazoMeses) {
      this.avisar('Indica el monto y el plazo.', true);
      return;
    }
    this.enviando.set(true);
    this.servicio.solicitarCredito(this.monto, this.plazoMeses).subscribe({
      next: s => {
        this.enviando.set(false);
        this.avisar(`Solicitud N° ${s.id} registrada. Cuota estimada: S/ ${s.cuotaMensual.toFixed(2)} al mes.`, false);
        this.monto = null;
        this.cargar();
      },
      error: e => {
        this.enviando.set(false);
        this.avisar(e.error?.error ?? 'No se pudo registrar la solicitud.', true);
      }
    });
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}
