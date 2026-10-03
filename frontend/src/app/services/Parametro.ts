import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Parametro {
  id: number;
  nombre: string;
  valor: string;
  descripcion: string;
}

@Injectable({ providedIn: 'root' })
export class ParametroService {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/api/parametros';

  listar(): Observable<Parametro[]> {
    return this.http.get<Parametro[]>(this.url);
  }

  actualizar(id: number, valor: string): Observable<any> {
    return this.http.put(`${this.url}/${id}`, { valor });
  }
}