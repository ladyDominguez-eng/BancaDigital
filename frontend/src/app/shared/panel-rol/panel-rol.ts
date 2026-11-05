import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth';

interface Enlace { ruta: string; texto: string; }

/**
 * Marco de la aplicación (barra lateral + contenido), común a los 5 roles.
 *
 * PROBLEMA: los paneles de Admin y Cajero tenían cada uno su propio HTML y
 *           CSS de la barra (más de 360 líneas repetidas) y se veían distinto
 *           al resto; Cliente, Asesor y Auditor usaban otro. Una persona que
 *           cambia de rol veía "otra aplicación".
 * SOLUCIÓN: un solo componente que recibe del enrutador (route data) el
 *           título del rol y sus enlaces. Agregar un rol nuevo es solo
 *           declarar su ruta, sin tocar este componente (O de SOLID). Los
 *           estilos viven en src/styles.css (.app-marco, .lateral).
 */
@Component({
  selector: 'app-panel-rol',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-rol.html'
})
export class PanelRol {
  private auth = inject(AuthService);
  private ruta = inject(ActivatedRoute);

  titulo: string = this.ruta.snapshot.data['titulo'] ?? '';
  enlaces: Enlace[] = this.ruta.snapshot.data['enlaces'] ?? [];
  usuario = this.auth.username;

  salir() { this.auth.salir(); }
}
