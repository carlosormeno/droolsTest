import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/rules', pathMatch: 'full' },
  {
    path: 'rules',
    loadComponent: () => import('./components/testReglas/rules-manager/rules-manager').then(m => m.RulesManager)
  },
  {
    path: 'simulator',
    loadComponent: () => import('./components/testReglas/client-simulator/client-simulator').then(m => m.ClientSimulator)
  },
  {
  path: 'evaluador',
  loadComponent: () => import('./components/parametricas/evaluador-parametricas/evaluador-parametricas').then(m => m.EvaluadorParametricas)
  },
  {
    path: 'uit-manager',
    loadComponent: () => import('./components/parametricas/uit-manager/uit-manager').then(m => m.UitManager)
  },
  {
    path: 'tipos-proceso-manager',
    loadComponent: () => import('./components/parametricas/tipos-proceso-manager/tipos-proceso-manager').then(m => m.TiposProcesoManager)
  },
  {
    path: 'objetos-contratacion-manager',
    loadComponent: () => import('./components/parametricas/objetos-contratacion-manager/objetos-contratacion-manager').then(m => m.ObjetosContratacionManager)
  },
  {
    path: 'operadores-monto-manager',
    loadComponent: () => import('./components/parametricas/operadores-monto-manager/operadores-monto-manager').then(m => m.OperadoresMontoManager)
  },
  {
    path: 'sub-descripcion-manager',
    loadComponent: () => import('./components/parametricas/sub-descripcion-manager/sub-descripcion-manager').then(m => m.SubDescripcionManager)
  },
  {
    path: 'topes-manager',
    loadComponent: () => import('./components/parametricas/topes-manager/topes-manager').then(m => m.TopesManager)
  }
];
