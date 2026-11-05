import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config';
import { Transaccion } from './cliente';

export interface FiltroTransacciones {
  tipo?: string;
  desde?: string;
  hasta?: string;
  montoMinimo?: number | null;
}

export interface AlertaRiesgo {
  regla: string;
  descripcion: string;
  transaccionId: number;
  cuenta: string;
  monto: number;
  fecha: string;
}

@Injectable({ providedIn: 'root' })
export class AuditorService {
  private http = inject(HttpClient);
  private url = `${API_URL}/auditor`;

  transacciones(f: FiltroTransacciones): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.url}/transacciones`, { params: this.params(f) });
  }

  riesgo(desde?: string, hasta?: string): Observable<AlertaRiesgo[]> {
    return this.http.get<AlertaRiesgo[]>(`${this.url}/riesgo`, { params: this.params({ desde, hasta }) });
  }

  /** Descarga el archivo; se hace con HttpClient (no con un enlace) para que lleve el token. */
  exportar(formato: string, f: FiltroTransacciones): Observable<Blob> {
    return this.http.get(`${this.url}/exportar`, {
      params: this.params(f).set('formato', formato),
      responseType: 'blob'
    });
  }

  // Un único lugar que arma los parámetros, ignorando los vacíos
  private params(f: FiltroTransacciones): HttpParams {
    let p = new HttpParams();
    if (f.tipo) p = p.set('tipo', f.tipo);
    if (f.desde) p = p.set('desde', f.desde);
    if (f.hasta) p = p.set('hasta', f.hasta);
    if (f.montoMinimo != null) p = p.set('montoMinimo', String(f.montoMinimo));
    return p;
  }
}
