import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private router = inject(Router);
  private auth = inject(AuthService);

  roles = [
    { valor: 'ADMIN',   texto: 'Administrador' },
    { valor: 'CLIENTE', texto: 'Cliente' },
    { valor: 'ASESOR',  texto: 'Asesor de Crédito' },
    { valor: 'CAJERO',  texto: 'Cajero' },
    { valor: 'AUDITOR', texto: 'Auditor' },
  ];

  // Pantalla inicial de cada rol
  private rutas: Record<string, string> = {
    ADMIN: '/admin', CLIENTE: '/cliente', ASESOR: '/asesor', CAJERO: '/cajero', AUDITOR: '/auditor',
  };

  rol = '';
  username = '';
  password = '';
  mensaje = '';

  ingresar() {
    if (!this.rol || !this.username || !this.password) {
      this.mensaje = 'Completa todos los campos.';
      return;
    }
    this.auth.iniciarSesion(this.username, this.password, this.rol).subscribe({
      next: respuesta => this.router.navigate([this.rutas[respuesta.rol] ?? '/login']),
      error: e => this.mensaje = e.error?.error ?? 'No se pudo conectar con el servidor.'
    });
  }
}
