import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './services/auth';

/**
 * Agrega el token a TODAS las peticiones al backend.
 *
 * PROBLEMA: el backend ahora exige el token en cada petición. Agregarlo a
 *           mano en cada servicio (6 archivos y los que vengan) sería copiar
 *           y pegar y fácil de olvidar.
 * SOLUCIÓN: un interceptor lo añade en un solo lugar. Si el servidor responde
 *           401 (sesión vencida) se cierra la sesión local y se vuelve al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const esLogin = req.url.endsWith('/login');
  const token = auth.token;

  const peticion = token && !esLogin
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError(error => {
      if (error.status === 401 && !esLogin) {
        auth.cerrarLocal();
      }
      return throwError(() => error);
    })
  );
};
