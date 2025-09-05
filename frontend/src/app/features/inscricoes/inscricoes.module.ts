import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./inscricoes-list/inscricoes-list.component').then(c => c.InscricoesListComponent)
  },
  {
    path: 'nova',
    loadComponent: () => import('./inscricao-form/inscricao-form.component').then(c => c.InscricaoFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./inscricao-detail/inscricao-detail.component').then(c => c.InscricaoDetailComponent)
  }
];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class InscricoesModule { }