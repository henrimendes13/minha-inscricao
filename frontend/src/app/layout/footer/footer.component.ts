import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule
  ],
  template: `
    <mat-toolbar class="footer-toolbar">
      <div class="footer-content">
        <span class="copyright">
          © 2025 Minha Inscrição - Sistema de Gerenciamento de Eventos Esportivos
        </span>
        <span class="version">
          v1.0.0
        </span>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .footer-toolbar {
      position: sticky;
      bottom: 0;
      background-color: #f5f5f5;
      color: rgba(0, 0, 0, 0.6);
      height: 48px;
      min-height: 48px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);
    }

    .footer-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      font-size: 12px;
    }

    .copyright {
      flex: 1;
    }

    .version {
      font-weight: 500;
    }

    @media (max-width: 600px) {
      .footer-content {
        flex-direction: column;
        gap: 4px;
        text-align: center;
      }
    }
  `]
})
export class FooterComponent { }