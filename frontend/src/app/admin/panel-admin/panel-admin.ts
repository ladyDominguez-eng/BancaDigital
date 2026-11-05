import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-panel-admin',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-admin.html',
  styleUrl: './panel-admin.css'
})


export class PanelAdmin {
  private auth = inject(AuthService);

  // Cierra la sesión en el servidor (el token deja de valer) y vuelve al login
  salir() { this.auth.salir(); }
}