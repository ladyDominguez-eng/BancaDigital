import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth';

interface Enlace { ruta: string; texto: string; }

/**
 * Barra superior + contenido, reutilizable por cualquier rol.
 *
 * PROBLEMA: los paneles de Admin y Cajero copiaron el mismo HTML y CSS de la
 *           barra superior; hacerlo otra vez para Cliente, Asesor y Auditor
 *           triplicaría el código repetido.
 * SOLUCIÓN: un solo componente que recibe del enrutador (route data) el
 *           título del rol y sus enlaces. Agregar un rol nuevo es solo
 *           declarar su ruta, sin tocar este componente.
 */
@Component({
  selector: 'app-panel-rol',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-rol.html',
  styleUrl: './panel-rol.css'
})
export class PanelRol {
  private auth = inject(AuthService);
  private ruta = inject(ActivatedRoute);

  titulo: string = this.ruta.snapshot.data['titulo'] ?? '';
  enlaces: Enlace[] = this.ruta.snapshot.data['enlaces'] ?? [];
  usuario = this.auth.username;

  salir() { this.auth.salir(); }
}
