import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioService, Usuario } from '../../services/usuario';

/**
 * Gestión de usuarios del personal (alta, cambio de rol, activar/desactivar).
 *
 * CORRECCIÓN: la lista y los mensajes eran propiedades normales. Esta
 * aplicación no usa zone.js (Angular 22 trabaja "sin zonas"), así que un
 * cambio que llega desde una respuesta HTTP no actualizaba la pantalla: la
 * lista no aparecía y los avisos no se veían hasta hacer otro clic. Con
 * signals, Angular sabe qué vista debe redibujar cuando el valor cambia.
 */
@Component({
  selector: 'app-usuarios',
  imports: [FormsModule],
  templateUrl: './usuarios.html'
})
export class Usuarios implements OnInit {
  private servicio = inject(UsuarioService);

  // Roles del personal. CLIENTE no está: un cliente necesita también sus datos
  // y su cuenta, y eso se hace en "Registrar cliente".
  roles = [
    { valor: 'ADMIN', texto: 'Administrador' },
    { valor: 'ASESOR', texto: 'Asesor de crédito' },
    { valor: 'CAJERO', texto: 'Cajero' },
    { valor: 'AUDITOR', texto: 'Auditor' }
  ];

  usuarios = signal<Usuario[]>([]);
  nuevo = { username: '', password: '', rol: '' };
  mensaje = signal('');
  esError = signal(false);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.servicio.listar().subscribe({
      next: lista => this.usuarios.set([...lista]),
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
      next: () => { this.avisar('Rol actualizado.', false); this.cargar(); },
      error: e => { this.avisar(e.error?.error ?? 'Error.', true); this.cargar(); }
    });
  }

  cambiarEstado(u: Usuario) {
    const nuevoEstado = !u.activo;

    this.servicio.cambiarEstado(u.id, nuevoEstado).subscribe({
      next: () => {
        this.avisar(nuevoEstado ? 'Usuario activado.' : 'Usuario desactivado.', false);
        this.cargar();
      },
      error: e => this.avisar(e.error?.error ?? 'Error al cambiar el estado.', true)
    });
  }

  private avisar(texto: string, error: boolean) {
    this.mensaje.set(texto);
    this.esError.set(error);
  }
}
