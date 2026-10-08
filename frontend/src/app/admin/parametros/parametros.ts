import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametroService, Parametro } from '../../services/parametro';

@Component({
  selector: 'app-parametros',
  imports: [FormsModule],
  templateUrl: './parametros.html',
  styleUrl: './parametros.css'
})
export class Parametros implements OnInit {

  private servicio = inject(ParametroService);

  parametros= signal<Parametro[]>([]);
  mensaje = signal('');
  esError = signal(false);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.servicio.listar().subscribe({
      next: lista => this.parametros.set(lista),   // CAMBIO
      error: () => this.avisar('No se pudo cargar la lista.', true)
    });
  }

  guardar(p: Parametro) {
    this.servicio.actualizar(p.id, p.valor).subscribe({
      next: () => this.avisar('Parámetro actualizado.', false),
      error: e => {
        this.avisar(e.error?.error ?? 'Error al guardar.', true);
        this.cargar();
      }
    });
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}