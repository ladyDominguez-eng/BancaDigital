import { Routes } from '@angular/router';

import { Login } from './login/login';

import { PanelAdmin } from './admin/panel-admin/panel-admin';
import { Usuarios } from './admin/usuarios/usuarios';
import { Parametros } from './admin/parametros/parametros';
import { Informes } from './admin/informes/informes';
import { Auditoria } from './admin/auditoria/auditoria';

import { PanelCajero } from './cajero/panel-cajero/panel-cajero';
import { Cuentas } from './cajero/cuentas/cuentas';
import { Depositos } from './cajero/depositos/depositos';

export const routes: Routes = [

  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: Login },

  {
    path: 'admin',
    component: PanelAdmin,
    children: [
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
      { path: 'usuarios', component: Usuarios },
      { path: 'parametros', component: Parametros },
      { path: 'informes', component: Informes },
      { path: 'auditoria', component: Auditoria }
    ]
  },
  
  {
    path: 'cajero',
    component: PanelCajero,
    children: [
      { path: '', redirectTo: 'cuentas', pathMatch: 'full' },
      { path: 'cuentas', component: Cuentas },
      { path: 'depositos', component: Depositos },
    ]
  },

];