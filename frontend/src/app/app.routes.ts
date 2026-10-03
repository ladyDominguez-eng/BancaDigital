import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Usuarios } from './admin/usuarios/usuarios';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'admin/usuarios', component: Usuarios },
];