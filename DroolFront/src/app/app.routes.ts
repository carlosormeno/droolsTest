import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/rules', pathMatch: 'full' },
  {
    path: 'rules',
    loadComponent: () => import('./components/rules-manager/rules-manager').then(m => m.RulesManager)
  },
  {
    path: 'simulator',
    loadComponent: () => import('./components/client-simulator/client-simulator').then(m => m.ClientSimulator)
  }
];
