import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./eventos-list/eventos-list.component').then(c => c.EventosListComponent)
  },
  {
    path: 'novo',
    loadComponent: () => import('./evento-form/evento-form.component').then(c => c.EventoFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./evento-detail/evento-detail.component').then(c => c.EventoDetailComponent)
  },
  {
    path: ':id/editar',
    loadComponent: () => import('./evento-form/evento-form.component').then(c => c.EventoFormComponent)
  }
];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class EventosModule { }
