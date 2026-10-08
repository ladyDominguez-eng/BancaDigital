import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuditoriaService, Bitacora } from '../../services/auditoria';

@Component({
  selector: 'app-auditoria',
  imports: [DatePipe],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.css'
})
export class Auditoria implements OnInit {

  private servicio = inject(AuditoriaService);

  registros = signal<Bitacora[]>([]);

  usuarioId = signal<number | undefined>(undefined);
  fechaDesde = signal('');
  fechaHasta = signal('');

  ngOnInit() {
    this.cargarAuditoria();
  }

  cargarAuditoria() {

    this.servicio.listar(
      this.usuarioId(),
      this.fechaDesde(),
      this.fechaHasta()
    ).subscribe({
      next: datos => {
        this.registros.set(datos);
      },
      error: error => {
        console.error('Error al cargar auditoría:', error);
      }
    });
  }

  limpiarFiltros() {

    this.usuarioId.set(undefined);
    this.fechaDesde.set('');
    this.fechaHasta.set('');

    this.cargarAuditoria();
  }
}