import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config';
import { Solicitud, Transaccion } from './cliente';

export interface HistorialCliente {
  solicitudes: Solicitud[];
  movimientos: Transaccion[];
}

@Injectable({ providedIn: 'root' })
export class AsesorService {
  private http = inject(HttpClient);
  private url = `${API_URL}/asesor`;

  solicitudes(estado?: string): Observable<Solicitud[]> {
    let params = new HttpParams();
    if (estado) params = params.set('estado', estado);
    return this.http.get<Solicitud[]>(`${this.url}/solicitudes`, { params });
  }

  evaluar(id: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.url}/solicitudes/${id}/evaluar`, {});
  }

  aprobar(id: number, observaciones: string): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.url}/solicitudes/${id}/aprobar`, { observaciones });
  }

  rechazar(id: number, observaciones: string): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.url}/solicitudes/${id}/rechazar`, { observaciones });
  }

  observar(id: number, observaciones: string): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.url}/solicitudes/${id}/observaciones`, { observaciones });
  }

  historial(clienteId: number): Observable<HistorialCliente> {
    return this.http.get<HistorialCliente>(`${this.url}/clientes/${clienteId}/historial`);
  }
}
