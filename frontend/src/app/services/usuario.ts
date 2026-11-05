import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config';

export interface Usuario {
  id: number;
  username: string;
  rol: string;
  activo: boolean;
}

/** Datos para dar de alta a un cliente con su primera cuenta. */
export interface NuevoCliente {
  username: string;
  password: string;
  dni: string;
  nombres: string;
  apellidos: string;
  tipoCuenta: string;
  saldoInicial: number | null;
}

export interface ClienteRegistrado {
  usuarioId: number;
  clienteId: number;
  cuentaId: number;
  numeroCuenta: string;
  comprobante: string | null;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private http = inject(HttpClient);
private url = `${API_URL}/usuarios`;

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.url);
  }

  crear(username: string, password: string, rol: string): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, { username, password, rol });
  }

  registrarCliente(datos: NuevoCliente): Observable<ClienteRegistrado> {
    return this.http.post<ClienteRegistrado>(`${this.url}/clientes`, datos);
  }

  cambiarRol(id: number, rol: string): Observable<any> {
    return this.http.put(`${this.url}/${id}/rol`, { rol });
  }

  cambiarEstado(id: number, activo: boolean): Observable<any> {
    return this.http.put(`${this.url}/${id}/estado`, { activo });
  }

}