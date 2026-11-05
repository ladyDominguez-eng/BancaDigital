import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config';

export interface CuentaCliente {
  id: number;
  numero: string;
  tipo: string;
  saldo: number;
  saldoDisponible: number;
  estado: string;
}

/** Una operación con su comprobante (la devuelve el backend como TransaccionVista). */
export interface Transaccion {
  id: number;
  tipo: string;
  monto: number;
  fecha: string;
  cuentaOrigen: string | null;
  cuentaDestino: string | null;
  comprobante: string | null;
}

export interface Solicitud {
  id: number;
  clienteId: number;
  clienteNombre: string;
  clienteDni: string;
  asesorId: number | null;
  monto: number;
  plazoMeses: number;
  estado: string;
  observaciones: string | null;
  fecha: string;
  cuotaMensual: number;
}

export interface Servicio { codigo: string; nombre: string; }

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private url = `${API_URL}/cliente`;

  cuentas(): Observable<CuentaCliente[]> {
    return this.http.get<CuentaCliente[]>(`${this.url}/cuentas`);
  }

  movimientos(cuentaId: number): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.url}/cuentas/${cuentaId}/movimientos`);
  }

  transferir(cuentaOrigenId: number, numeroDestino: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.url}/transferencias`, { cuentaOrigenId, numeroDestino, monto });
  }

  servicios(): Observable<Servicio[]> {
    return this.http.get<Servicio[]>(`${this.url}/servicios`);
  }

  pagar(cuentaId: number, servicio: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.url}/pagos`, { cuentaId, servicio, monto });
  }

  creditos(): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${this.url}/creditos`);
  }

  solicitarCredito(monto: number, plazoMeses: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.url}/creditos`, { monto, plazoMeses });
  }
}
