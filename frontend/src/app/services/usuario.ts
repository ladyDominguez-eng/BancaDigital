import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Usuario {
  id: number;
  username: string;
  rol: string;
  activo: boolean;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private http = inject(HttpClient);

  private url = 'http://localhost:8080/api/usuarios';

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.url);
  }

  crear(username: string, password: string, rol: string): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, { username, password, rol });
  }

  cambiarRol(id: number, rol: string): Observable<any> {
    return this.http.put(`${this.url}/${id}/rol`, { rol });
  }

  cambiarEstado(id: number, activo: boolean): Observable<any> {
    return this.http.put(`${this.url}/${id}/estado`, { activo });
  }

}