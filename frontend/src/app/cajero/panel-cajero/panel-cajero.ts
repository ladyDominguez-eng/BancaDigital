import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-panel-cajero',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-cajero.html',
  styleUrl: './panel-cajero.css'
})
export class PanelCajero {
  private router = inject(Router);

  salir() { this.router.navigate(['/login']); }
}