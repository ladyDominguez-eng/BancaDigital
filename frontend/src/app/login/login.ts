import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  roles = [
    { valor: 'ADMIN',   texto: 'Administrador' },
    { valor: 'CLIENTE', texto: 'Cliente' },
    { valor: 'ASESOR',  texto: 'Asesor de Crédito' },
    { valor: 'CAJERO',  texto: 'Cajero' },
    { valor: 'AUDITOR', texto: 'Auditor' },
  ];

  rol = '';
  username = '';
  password = '';
  mensaje = '';

  ingresar() {
    if (!this.rol || !this.username || !this.password) {
      this.mensaje = 'Completa todos los campos.';
      return;
    }
    this.mensaje = '';
    console.log(this.rol, this.username);
  }
}
