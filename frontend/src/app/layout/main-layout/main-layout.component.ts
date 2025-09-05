import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    HeaderComponent,
    SidebarComponent,
    FooterComponent
  ],
  template: `
    <div class="main-layout">
      <app-header (toggleSidebar)="toggleSidebar()"></app-header>
      
      <mat-sidenav-container class="sidenav-container">
        <mat-sidenav 
          #drawer 
          class="sidenav"
          [mode]="isHandset ? 'over' : 'side'"
          [opened]="!isHandset"
          [fixedInViewport]="isHandset">
          <app-sidebar [isOpen]="drawer.opened"></app-sidebar>
        </mat-sidenav>
        
        <mat-sidenav-content class="sidenav-content">
          <div class="content-wrapper">
            <main class="main-content">
              <router-outlet></router-outlet>
            </main>
            <app-footer></app-footer>
          </div>
        </mat-sidenav-content>
      </mat-sidenav-container>
    </div>
  `,
  styles: [`
    .main-layout {
      display: flex;
      flex-direction: column;
      height: 100vh;
    }

    .sidenav-container {
      flex: 1;
      display: flex;
    }

    .sidenav {
      width: 280px;
      border-right: 1px solid rgba(0, 0, 0, 0.12);
    }

    .sidenav-content {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .content-wrapper {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 0;
    }

    .main-content {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
      background-color: #fafafa;
    }

    @media (max-width: 960px) {
      .main-content {
        padding: 16px;
      }
    }

    @media (max-width: 600px) {
      .main-content {
        padding: 8px;
      }
    }
  `]
})
export class MainLayoutComponent {
  @ViewChild('drawer', { static: true }) drawer!: MatSidenav;
  
  isHandset = false;

  constructor(private breakpointObserver: BreakpointObserver) {
    this.breakpointObserver.observe(Breakpoints.Handset)
      .subscribe(result => {
        this.isHandset = result.matches;
      });
  }

  toggleSidebar(): void {
    this.drawer.toggle();
  }
}