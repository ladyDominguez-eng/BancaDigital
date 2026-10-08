import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InformesService {

  private http = inject(HttpClient);

  private url = 'http://localhost:8080/api/informes';

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