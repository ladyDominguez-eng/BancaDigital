import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-panel-cajero',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-cajero.html',
  styleUrl: './panel-cajero.css'
})
export class PanelCajero {
  private auth = inject(AuthService);

  // Cierra la sesión en el servidor (el token deja de valer) y vuelve al login
  salir() { this.auth.salir(); }
}