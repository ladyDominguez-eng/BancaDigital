import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private router = inject(Router);
  private http = inject(HttpClient);

  roles = [
    { valor: 'ADMIN',   texto: 'Administrador' },
    { valor: 'CLIENTE', texto: 'Cliente' },
    { valor: 'ASESOR',  texto: 'Asesor de Crédito' },
    { valor: 'CAJERO',  texto: 'Cajero' },
  ];

  private rutas: Record<string, string> = {
    ADMIN: '/admin', CLIENTE: '/cliente', ASESOR: '/asesor', CAJERO: '/cajero',
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
    this.http.post('http://localhost:8080/api/login', {
  username: this.username,
  password: this.password,
  rol: this.rol
}).subscribe({
  next: (respuesta) => {
    console.log('✅ LOGIN CORRECTO:', respuesta);
    console.log('➡️ ROL:', this.rol);
    console.log('➡️ RUTA:', this.rutas[this.rol]);

    this.router.navigate([this.rutas[this.rol]]);
  },
  error: e => this.mensaje = e.error?.error ?? 'No se pudo conectar con el servidor.'
});
  }
}