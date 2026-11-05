import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config';

@Injectable({
  providedIn: 'root'
})
export class InformesService {

  private http = inject(HttpClient);


private url = `${API_URL}/informes`;

  usuariosPorRol(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/usuarios`);
  }

  resumenCuentas(): Observable<any> {
    return this.http.get<any>(`${this.url}/cuentas`);
  }

  transaccionesPorTipo(): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/transacciones`);
  }
}