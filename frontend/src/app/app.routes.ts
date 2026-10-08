import { Routes } from '@angular/router';

import { Login } from './login/login';
import { PanelAdmin } from './admin/panel-admin/panel-admin';
import { Usuarios } from './admin/usuarios/usuarios';
import { Parametros } from './admin/parametros/parametros';
import { Informes } from './admin/informes/informes';

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
      { path: 'informes', component: Informes }
    
    ]
  }

];