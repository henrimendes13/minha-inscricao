import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./atletas-list/atletas-list.component').then(c => c.AtletasListComponent)
  },
  {
    path: 'novo',
    loadComponent: () => import('./atleta-form/atleta-form.component').then(c => c.AtletaFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./atleta-detail/atleta-detail.component').then(c => c.AtletaDetailComponent)
  }
];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class AtletasModule { }