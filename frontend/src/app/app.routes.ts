import { Routes } from '@angular/router';

import { rolGuard } from './rol.guard';
import { PanelRol } from './shared/panel-rol/panel-rol';

import { Login } from './login/login';

import { PanelAdmin } from './admin/panel-admin/panel-admin';
import { Usuarios } from './admin/usuarios/usuarios';
import { Parametros } from './admin/parametros/parametros';
import { Informes } from './admin/informes/informes';
import { Auditoria } from './admin/auditoria/auditoria';

import { PanelCajero } from './cajero/panel-cajero/panel-cajero';
import { Cuentas } from './cajero/cuentas/cuentas';
import { Depositos } from './cajero/depositos/depositos';
import { Retiros } from './cajero/retiros/retiros';
import { Comprobantes } from './cajero/comprobantes/comprobantes';

import { MisCuentas } from './cliente/mis-cuentas/mis-cuentas';
import { Transferir } from './cliente/transferir/transferir';
import { Pagos } from './cliente/pagos/pagos';
import { Creditos } from './cliente/creditos/creditos';

import { Solicitudes } from './asesor/solicitudes/solicitudes';

import { Transacciones } from './auditor/transacciones/transacciones';
import { Riesgo } from './auditor/riesgo/riesgo';

/*
 * Cada grupo de rutas lleva su guard de rol: sin la sesión del rol
 * correspondiente se vuelve al login. Los paneles de Cliente, Asesor y
 * Auditor usan el mismo componente PanelRol; solo cambian su título y sus
 * enlaces (route data), así no se repite la barra superior.
 */
export const routes: Routes = [

  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: Login },

  {
    path: 'admin',
    component: PanelAdmin,
    canActivate: [rolGuard('ADMIN')],
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
    canActivate: [rolGuard('CAJERO')],
    children: [
      { path: '', redirectTo: 'cuentas', pathMatch: 'full' },
      { path: 'cuentas', component: Cuentas },
      { path: 'depositos', component: Depositos },
      { path: 'retiros', component: Retiros },
      { path: 'comprobantes', component: Comprobantes },
    ]
  },

  {
    path: 'cliente',
    component: PanelRol,
    canActivate: [rolGuard('CLIENTE')],
    data: {
      titulo: 'Cliente',
      enlaces: [
        { ruta: '/cliente/cuentas', texto: 'Mis cuentas' },
        { ruta: '/cliente/transferir', texto: 'Transferir' },
        { ruta: '/cliente/pagos', texto: 'Pagar servicios' },
        { ruta: '/cliente/creditos', texto: 'Créditos' },
      ]
    },
    children: [
      { path: '', redirectTo: 'cuentas', pathMatch: 'full' },
      { path: 'cuentas', component: MisCuentas },
      { path: 'transferir', component: Transferir },
      { path: 'pagos', component: Pagos },
      { path: 'creditos', component: Creditos },
    ]
  },

  {
    path: 'asesor',
    component: PanelRol,
    canActivate: [rolGuard('ASESOR')],
    data: {
      titulo: 'Asesor de Crédito',
      enlaces: [{ ruta: '/asesor/solicitudes', texto: 'Solicitudes' }]
    },
    children: [
      { path: '', redirectTo: 'solicitudes', pathMatch: 'full' },
      { path: 'solicitudes', component: Solicitudes },
    ]
  },

  {
    path: 'auditor',
    component: PanelRol,
    canActivate: [rolGuard('AUDITOR')],
    data: {
      titulo: 'Auditor',
      enlaces: [
        { ruta: '/auditor/transacciones', texto: 'Transacciones' },
        { ruta: '/auditor/riesgo', texto: 'Riesgo' },
        { ruta: '/auditor/bitacora', texto: 'Bitácora' },
        { ruta: '/auditor/informes', texto: 'Informes' },
      ]
    },
    children: [
      { path: '', redirectTo: 'transacciones', pathMatch: 'full' },
      { path: 'transacciones', component: Transacciones },
      { path: 'riesgo', component: Riesgo },
      // Se reutilizan las pantallas del administrador: el backend ya permite el rol AUDITOR
      { path: 'bitacora', component: Auditoria },
      { path: 'informes', component: Informes },
    ]
  },

  // Cualquier otra dirección lleva al login
  { path: '**', redirectTo: 'login' },
];
