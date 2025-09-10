import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { EventoOwnerGuard } from './core/guards/evento-owner.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/eventos',
    pathMatch: 'full'
  },
  {
    path: 'eventos',
    loadComponent: () => import('./features/eventos/eventos-list/eventos-list.component').then(c => c.EventosListComponent)
  },
  {
    path: 'eventos/:id',
    loadComponent: () => import('./features/eventos/evento-detalhes/evento-detalhes.component').then(c => c.EventoDetalhesComponent)
  },
  {
    path: 'eventos/:eventoId/categoria/:categoriaId/resultados',
    loadComponent: () => import('./features/workout-resultados/workout-resultados-manage/workout-resultados-manage.component').then(c => c.WorkoutResultadosManageComponent),
    canActivate: [EventoOwnerGuard]
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(c => c.LoginComponent)
  },
  {
    path: 'perfil',
    loadComponent: () => import('./features/eventos/eventos-list/eventos-list.component').then(c => c.EventosListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'minhas-inscricoes',
    loadComponent: () => import('./features/eventos/eventos-list/eventos-list.component').then(c => c.EventosListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(c => c.MainLayoutComponent),
    canActivate: [AuthGuard],
    data: { requiredRole: 'ADMIN' },
    children: [
      {
        path: '',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(c => c.DashboardComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/eventos'
  }
];
