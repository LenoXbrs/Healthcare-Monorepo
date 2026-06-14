import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login').then(m => m.Login) },
  { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard) },
  { path: 'triagem', loadComponent: () => import('./features/triage-form/triage-form').then(m => m.TriageForm) },
  { path: 'atendimento', loadComponent: () => import('./features/consultation/consultation').then(m => m.Consultation) },
  { path: 'mural', loadComponent: () => import('./features/waiting-board/waiting-board').then(m => m.WaitingBoard) },
  { path: 'relatorios', loadComponent: () => import('./features/reports/reports').then(m => m.Reports) },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
