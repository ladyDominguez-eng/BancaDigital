import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioService, Usuario } from '../../services/usuario';

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios implements OnInit {
  private servicio = inject(UsuarioService);

  roles = ['ADMIN', 'CLIENTE', 'ASESOR', 'CAJERO'];
  usuarios: Usuario[] = [];
  nuevo = { username: '', password: '', rol: '' };
  mensaje = '';
  esError = false;

 ngOnInit() {
  this.cargar();
}

cargar() {
  this.servicio.listar().subscribe({
    next: lista => {
      console.log('USUARIOS RECIBIDOS:', lista);
      this.usuarios = [...lista];
    },
    error: () => this.avisar('No se pudo cargar la lista.', true)
  });

}

  crear() {
    this.servicio.crear(this.nuevo.username, this.nuevo.password, this.nuevo.rol).subscribe({
      next: () => {
        this.avisar('Usuario creado.', false);
        this.nuevo = { username: '', password: '', rol: '' };
        this.cargar();
      },
      error: e => this.avisar(e.error?.error ?? 'Error al crear.', true)
    });
  }

  cambiarRol(u: Usuario, rol: string) {
    this.servicio.cambiarRol(u.id, rol).subscribe({
      next: () => this.avisar('Rol actualizado.', false),
      error: e => { this.avisar(e.error?.error ?? 'Error.', true); this.cargar(); }
    });
  }

  eliminar(id: number) {
  this.servicio.eliminar(id).subscribe({
    next: () => {
      this.avisar('Usuario eliminado.', false);
      this.cargar();
    },
    error: e => this.avisar(e.error?.error ?? 'Error al eliminar.', true)
  });
}

  private avisar(texto: string, error: boolean) {
    this.mensaje = texto;
    this.esError = error;
  }
}