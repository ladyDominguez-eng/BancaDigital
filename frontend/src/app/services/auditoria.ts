import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Bitacora {
  id: number;
  usuarioId: number;
  username: string;
  rol: string;
  accion: string;
  fecha: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {

  private http = inject(HttpClient);

  private url = 'http://localhost:8080/api/auditoria';

  listar(
    usuarioId?: number,
    fechaDesde?: string,
    fechaHasta?: string
  ): Observable<Bitacora[]> {

    const params: any = {};

    if (usuarioId) {
      params.usuarioId = usuarioId;
    }

    if (fechaDesde) {
      params.fechaDesde = fechaDesde;
    }

    if (fechaHasta) {
      params.fechaHasta = fechaHasta;
    }

    return this.http.get<Bitacora[]>(this.url, { params });
  }
}