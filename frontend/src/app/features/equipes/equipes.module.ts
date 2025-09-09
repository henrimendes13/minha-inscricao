import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./equipes-list/equipes-list.component').then(c => c.EquipesListComponent)
  },
  {
    path: 'nova',
    loadComponent: () => import('./equipe-form/equipe-form.component').then(c => c.EquipeFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./equipe-detail/equipe-detail.component').then(c => c.EquipeDetailComponent)
  }
];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class EquipesModule { }
