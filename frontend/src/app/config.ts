/**
 * URL base de la API.
 *
 * PROBLEMA: la dirección del backend de Render estaba escrita a mano en 6
 *           archivos distintos; para probar con el backend local había que
 *           cambiar los 6 (y era fácil subir uno por error).
 * SOLUCIÓN: un solo lugar. Si la página se abre desde localhost se usa el
 *           backend local (puerto 8080); en cualquier otro caso, el de Render.
 */
const enLocal = ['localhost', '127.0.0.1'].includes(window.location.hostname);

export const API_URL = enLocal
  ? 'http://localhost:8080/api'
  : 'https://bancadigital-1.onrender.com/api';
