import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config';
import { Transaccion } from './cliente';

export interface CuentaDto {
  id: number;
  numero: string;
  titular: string;
  dni: string;
  tipo: string;
  saldo: number;
  saldoDisponible: number;
  estado: string;
}

@Injectable({ providedIn: 'root' })
export class CajeroService {
  private http = inject(HttpClient);
private url = `${API_URL}/cajero`;

  consultar(dni: string, numero: string): Observable<CuentaDto[]> {
    let params = new HttpParams();
    if (dni) params = params.set('dni', dni);
    if (numero) params = params.set('numero', numero);
    return this.http.get<CuentaDto[]>(`${this.url}/cuentas`, { params });
  }

  depositar(numeroCuenta: string, monto: number): Observable<any> {
    return this.http.post(`${this.url}/depositos`, { numeroCuenta, monto });
  }

  // Retiro: mismos datos que un depósito (cuenta y monto)
  retirar(numeroCuenta: string, monto: number): Observable<any> {
    return this.http.post(`${this.url}/retiros`, { numeroCuenta, monto });
  }

  // Consulta un comprobante emitido por su código
  comprobante(codigo: string): Observable<Transaccion> {
    return this.http.get<Transaccion>(`${this.url}/comprobantes/${encodeURIComponent(codigo)}`);
  }
}