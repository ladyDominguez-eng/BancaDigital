import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../config';

export interface RespuestaLogin {
  success: boolean;
  rol: string;
  username: string;
  token: string;
}

interface Sesion {
  token: string;
  rol: string;
  username: string;
}

/**
 * Guarda la sesión del usuario (token, rol y nombre) y cierra la sesión.
 *
 * Se usa sessionStorage: la sesión se borra al cerrar la pestaña. El token
 * lo valida el servidor en cada petición (ver auth.interceptor.ts); lo que
 * se guarda aquí solo sirve para decidir qué pantallas mostrar.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private readonly CLAVE = 'banca.sesion';

  iniciarSesion(username: string, password: string, rol: string): Observable<RespuestaLogin> {
    return this.http
      .post<RespuestaLogin>(`${API_URL}/login`, { username, password, rol })
      .pipe(tap(r => this.guardar({ token: r.token, rol: r.rol, username: r.username })));
  }

  get token(): string | null { return this.leer()?.token ?? null; }
  get rol(): string | null { return this.leer()?.rol ?? null; }
  get username(): string | null { return this.leer()?.username ?? null; }

  /** Cierra la sesión en el servidor (si se puede) y vuelve al login. */
  salir(): void {
    if (this.token) {
      // Si falla (servidor caído) no importa: igual se borra la sesión local
      this.http.post(`${API_URL}/logout`, {}).subscribe({ error: () => {} });
    }
    this.cerrarLocal();
  }

  /** Borra la sesión local y manda al login (también cuando el token venció). */
  cerrarLocal(): void {
    try { sessionStorage.removeItem(this.CLAVE); } catch { /* sin almacenamiento */ }
    this.router.navigate(['/login']);
  }

  private guardar(s: Sesion): void {
    try { sessionStorage.setItem(this.CLAVE, JSON.stringify(s)); } catch { /* sin almacenamiento */ }
  }

  private leer(): Sesion | null {
    try {
      const texto = sessionStorage.getItem(this.CLAVE);
      return texto ? JSON.parse(texto) as Sesion : null;
    } catch {
      return null;
    }
  }
}
