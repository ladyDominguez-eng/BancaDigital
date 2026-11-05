import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlertaRiesgo, AuditorService } from '../../services/auditor';

/** Reporte de riesgo: operaciones que las reglas del backend marcan como sospechosas. */
@Component({
  selector: 'app-riesgo',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './riesgo.html'
})
export class Riesgo implements OnInit {
  private servicio = inject(AuditorService);

  desde = '';
  hasta = '';
  alertas = signal<AlertaRiesgo[]>([]);
  mensaje = signal('');
  consultado = signal(false);

  ngOnInit() { this.generar(); }

  generar() {
    this.servicio.riesgo(this.desde, this.hasta).subscribe({
      next: lista => {
        this.alertas.set(lista);
        this.consultado.set(true);
        this.mensaje.set('');
      },
      error: e => {
        this.alertas.set([]);
        this.mensaje.set(e.error?.error ?? 'No se pudo generar el reporte.');
      }
    });
  }

  // Nombre legible de la regla que generó la alerta
  nombreRegla(regla: string): string {
    return regla === 'MONTO_ALTO' ? 'Monto alto'
         : regla === 'OPERACIONES_FRECUENTES' ? 'Operaciones frecuentes'
         : regla;
  }
}
