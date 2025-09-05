import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';

import { AuthService } from '../../core/auth/auth.service';
import { TipoUsuario } from '../../models';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: TipoUsuario[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatListModule,
    MatIconModule,
    MatSidenavModule
  ],
  template: `
    <mat-nav-list class="nav-list">
      <div class="nav-header">
        <h3>Menu</h3>
      </div>
      
      <mat-list-item 
        *ngFor="let item of visibleNavItems" 
        [routerLink]="item.route"
        routerLinkActive="active-nav-item"
        class="nav-item">
        <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
        <span matListItemTitle>{{ item.label }}</span>
      </mat-list-item>
    </mat-nav-list>
  `,
  styles: [`
    .nav-list {
      padding: 0;
      height: 100%;
    }

    .nav-header {
      padding: 16px;
      background-color: rgba(0, 0, 0, 0.04);
      margin-bottom: 8px;
    }

    .nav-header h3 {
      margin: 0;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
    }

    .nav-item {
      border-radius: 0 25px 25px 0;
      margin: 4px 8px 4px 0;
      padding: 12px 16px;
      transition: all 0.2s ease-in-out;
    }

    .nav-item:hover {
      background-color: rgba(0, 0, 0, 0.04);
    }

    .active-nav-item {
      background-color: #e3f2fd;
      color: #1976d2;
    }

    .active-nav-item mat-icon {
      color: #1976d2;
    }

    .nav-item mat-icon {
      margin-right: 16px;
    }
  `]
})
export class SidebarComponent {
  @Input() isOpen = true;

  private navItems: NavItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard'
    },
    {
      label: 'Eventos',
      icon: 'event',
      route: '/eventos',
      roles: [TipoUsuario.ORGANIZADOR, TipoUsuario.ADMIN]
    },
    {
      label: 'Minhas Inscrições',
      icon: 'assignment',
      route: '/inscricoes',
      roles: [TipoUsuario.ATLETA]
    },
    {
      label: 'Todas as Inscrições',
      icon: 'list_alt',
      route: '/inscricoes',
      roles: [TipoUsuario.ORGANIZADOR, TipoUsuario.ADMIN]
    },
    {
      label: 'Atletas',
      icon: 'group',
      route: '/atletas'
    },
    {
      label: 'Equipes',
      icon: 'groups',
      route: '/equipes'
    },
    {
      label: 'Leaderboard',
      icon: 'leaderboard',
      route: '/leaderboard'
    }
  ];

  get visibleNavItems(): NavItem[] {
    const currentUser = this.authService.getCurrentUser();
    
    if (!currentUser) {
      return [];
    }

    return this.navItems.filter(item => {
      if (!item.roles || item.roles.length === 0) {
        return true;
      }
      return item.roles.includes(currentUser.tipoUsuario as TipoUsuario);
    });
  }

  constructor(private authService: AuthService) {}
}