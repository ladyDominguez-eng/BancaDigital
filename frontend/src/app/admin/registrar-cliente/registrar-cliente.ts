import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClienteRegistrado, NuevoCliente, UsuarioService } from '../../services/usuario';

/**
 * Alta de un cliente nuevo desde el Administrador: acceso, datos personales y
 * primera cuenta en un solo paso (el backend lo guarda todo junto o nada).
 *
 * PROBLEMA: antes había que insertar a mano en tres tablas de la base.
 * SOLUCIÓN: un formulario con tres grupos (Gestalt, proximidad): quién es,
 * cómo entra y qué cuenta recibe. Los errores del servidor se muestran tal
 * cual porque ya están redactados para la persona (por ejemplo "Ya existe un
 * cliente con ese DNI.").
 */
@Component({
  selector: 'app-registrar-cliente',
  imports: [FormsModule],
  templateUrl: './registrar-cliente.html'
})
export class RegistrarCliente {
  private servicio = inject(UsuarioService);

  tiposCuenta = [
    { valor: 'AHORRO', texto: 'Ahorro' },
    { valor: 'CORRIENTE', texto: 'Corriente' }
  ];

  datos: NuevoCliente = this.vacio();
  enviando = signal(false);
  error = signal('');
  registrado = signal<ClienteRegistrado | null>(null);
  nombreRegistrado = signal('');

  registrar() {
    this.error.set('');
    this.registrado.set(null);
    this.enviando.set(true);

    // Lo que se muestra en el aviso de éxito: se guarda antes de limpiar el formulario
    const nombre = `${this.datos.nombres.trim()} ${this.datos.apellidos.trim()}`;
    this.servicio.registrarCliente({
      ...this.datos,
      saldoInicial: this.datos.saldoInicial || null
    }).subscribe({
      next: r => {
        this.registrado.set(r);
        this.nombreRegistrado.set(nombre);
        this.datos = this.vacio();
        this.enviando.set(false);
      },
      error: e => {
        this.error.set(e.error?.error ?? 'No se pudo registrar al cliente. Intenta de nuevo.');
        this.enviando.set(false);
      }
    });
  }

  private vacio(): NuevoCliente {
    return { username: '', password: '', dni: '', nombres: '', apellidos: '', tipoCuenta: 'AHORRO', saldoInicial: null };
  }
}
