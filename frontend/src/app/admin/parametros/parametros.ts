import { Component, OnInit, inject } from '@angular/core';
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

  parametros: Parametro[] = [];
  mensaje = '';
  esError = false;

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.servicio.listar().subscribe({
      next: lista => {
        console.log('DATOS RECIBIDOS:', lista);
        this.parametros = lista;
      },
      error: error => {
        console.error('ERROR:', error);
        this.avisar('No se pudo cargar la lista.', true);
      }
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
    this.mensaje = texto;
    this.esError = error;
  }
}