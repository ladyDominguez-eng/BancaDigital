import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './services/auth';

/**
 * Protege un grupo de rutas: solo entra quien tiene el rol indicado.
 *
 * PROBLEMA: cualquiera podía escribir /admin en la barra de direcciones y
 *           ver la pantalla sin haber iniciado sesión.
 * SOLUCIÓN: este guard comprueba el rol antes de mostrar la ruta; si no
 *           corresponde, lleva al login. (La seguridad real está en el
 *           backend, que valida el token; esto evita mostrar pantallas
 *           que el usuario no puede usar.)
 */
export const rolGuard = (rolPermitido: string): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.rol === rolPermitido ? true : router.createUrlTree(['/login']);
};
