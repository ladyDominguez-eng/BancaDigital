import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  usuario: string = '';
  clave: string = '';
  rol: string = '';

  login() {
    alert(`Bienvenido ${this.rol} ${this.usuario} a BancaDigital ✨`);
  }
}


