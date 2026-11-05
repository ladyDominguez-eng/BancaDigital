import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditorService, FiltroTransacciones } from '../../services/auditor';
import { Transaccion } from '../../services/cliente';

/** RF-12 y RF-15: el auditor revisa las transacciones y las exporta. */
@Component({
  selector: 'app-transacciones',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './transacciones.html'
})
export class Transacciones implements OnInit {
  private servicio = inject(AuditorService);

  tipos = ['DEPOSITO', 'RETIRO', 'TRANSFERENCIA', 'PAGO_SERVICIO'];
  formatos = [
    { valor: 'csv', texto: 'CSV' },
    { valor: 'excel', texto: 'CSV para Excel' }
  ];

  tipo = '';
  desde = '';
  hasta = '';
  montoMinimo: number | null = null;
  formato = 'csv';

  lista = signal<Transaccion[]>([]);
  mensaje = signal('');
  esError = signal(false);

  ngOnInit() { this.buscar(); }

  private filtro(): FiltroTransacciones {
    return { tipo: this.tipo, desde: this.desde, hasta: this.hasta, montoMinimo: this.montoMinimo };
  }

  buscar() {
    this.servicio.transacciones(this.filtro()).subscribe({
      next: l => {
        this.lista.set(l);
        this.avisar(l.length ? '' : 'No hay transacciones con esos filtros.', l.length === 0);
      },
      error: e => {
        this.lista.set([]);
        this.avisar(e.error?.error ?? 'No se pudieron consultar las transacciones.', true);
      }
    });
  }

  limpiar() {
    this.tipo = ''; this.desde = ''; this.hasta = ''; this.montoMinimo = null;
    this.buscar();
  }

  exportar() {
    this.servicio.exportar(this.formato, this.filtro()).subscribe({
      next: blob => {
        // Se crea un enlace temporal para que el navegador guarde el archivo
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `transacciones-${new Date().toISOString().slice(0, 10)}.csv`;
        enlace.click();
        URL.revokeObjectURL(url);
        this.avisar('Exportación lista.', false);
      },
      // Con responseType blob el error llega como Blob: se lee el texto para mostrarlo
      error: async e => {
        let texto = 'No se pudo exportar.';
        try { texto = JSON.parse(await e.error.text()).error ?? texto; } catch { /* mensaje genérico */ }
        this.avisar(texto, true);
      }
    });
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}
