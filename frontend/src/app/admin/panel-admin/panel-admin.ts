import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-panel-admin',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-admin.html',
  styleUrl: './panel-admin.css'
})
export class PanelAdmin {
  private router = inject(Router);

  salir() { this.router.navigate(['/login']); }
}