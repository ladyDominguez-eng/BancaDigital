import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { AsesorService, HistorialCliente } from '../../services/asesor';
import { Solicitud } from '../../services/cliente';

/**
 * RF-07, RF-08, RF-11: el asesor revisa las solicitudes, las evalúa, las
 * aprueba o rechaza y deja observaciones.
 *
 * Los botones se habilitan según el estado solo para guiar al usuario; la
 * regla real (qué acción permite cada estado) la aplica el backend con el
 * patrón State, que responde con error si la acción no corresponde.
 */
@Component({
  selector: 'app-solicitudes',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './solicitudes.html'
})
export class Solicitudes implements OnInit {
  private servicio = inject(AsesorService);

  estados = ['PENDIENTE', 'EN_EVALUACION', 'APROBADO', 'RECHAZADO'];
  filtro = '';

  solicitudes = signal<Solicitud[]>([]);
  seleccionada = signal<Solicitud | null>(null);
  historial = signal<HistorialCliente | null>(null);
  mensaje = signal('');
  esError = signal(false);

  observaciones = '';

  ngOnInit() { this.cargar(); }

  cargar() {
    this.servicio.solicitudes(this.filtro || undefined).subscribe({
      next: lista => {
        this.solicitudes.set(lista);
        // Si la solicitud abierta cambió, se actualiza su detalle
        const actual = this.seleccionada();
        if (actual) {
          const nueva = lista.find(s => s.id === actual.id);
          this.seleccionada.set(nueva ?? null);
        }
      },
      error: e => this.avisar(e.error?.error ?? 'No se pudieron cargar las solicitudes.', true)
    });
  }

  abrir(s: Solicitud) {
    this.seleccionada.set(s);
    this.observaciones = s.observaciones ?? '';
    this.historial.set(null);
    this.avisar('', false);
    this.servicio.historial(s.clienteId).subscribe({
      next: h => this.historial.set(h),
      error: e => this.avisar(e.error?.error ?? 'No se pudo cargar el historial.', true)
    });
  }

  evaluar()  { this.ejecutar(s => this.servicio.evaluar(s.id), 'La solicitud pasó a evaluación.'); }
  aprobar()  { this.ejecutar(s => this.servicio.aprobar(s.id, this.observaciones), 'Solicitud aprobada.'); }
  rechazar() { this.ejecutar(s => this.servicio.rechazar(s.id, this.observaciones), 'Solicitud rechazada.'); }

  guardarObservacion() {
    this.ejecutar(s => this.servicio.observar(s.id, this.observaciones), 'Observación guardada.');
  }

  // Un único lugar para llamar al backend, mostrar el resultado y refrescar la lista
  private ejecutar(accion: (s: Solicitud) => Observable<Solicitud>, exito: string) {
    const s = this.seleccionada();
    if (!s) return;
    accion(s).subscribe({
      next: actualizada => {
        this.seleccionada.set(actualizada);
        this.avisar(exito, false);
        this.cargar();
      },
      error: e => {
        this.avisar(e.error?.error ?? 'No se pudo completar la acción.', true);
        this.cargar(); // por si otro asesor cambió el estado
      }
    });
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}
