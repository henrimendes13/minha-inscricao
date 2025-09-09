import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { AuthService } from '../../core/auth/auth.service';
import { UsuarioResponseDTO } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule
  ],
  template: `
    <div class="dashboard-container">
      <div class="welcome-section">
        <h1>Bem-vindo ao Sistema de Inscrições Esportivas!</h1>
        <p *ngIf="currentUser" class="user-welcome">
          Olá, <strong>{{ currentUser.nome }}</strong>! 
          Você está logado como <span class="user-role">{{ currentUser.tipoUsuario }}</span>.
        </p>
      </div>

      <div class="cards-grid">
        <mat-card class="dashboard-card">
          <mat-card-header>
            <mat-icon mat-card-avatar>event</mat-icon>
            <mat-card-title>Eventos</mat-card-title>
            <mat-card-subtitle>Gerencie eventos esportivos</mat-card-subtitle>
          </mat-card-header>
          <mat-card-actions>
            <button mat-button color="primary">Ver Eventos</button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="dashboard-card">
          <mat-card-header>
            <mat-icon mat-card-avatar>assignment</mat-icon>
            <mat-card-title>Inscrições</mat-card-title>
            <mat-card-subtitle>Acompanhe suas inscrições</mat-card-subtitle>
          </mat-card-header>
          <mat-card-actions>
            <button mat-button color="primary">Ver Inscrições</button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="dashboard-card">
          <mat-card-header>
            <mat-icon mat-card-avatar>group</mat-icon>
            <mat-card-title>Atletas</mat-card-title>
            <mat-card-subtitle>Gerencie atletas</mat-card-subtitle>
          </mat-card-header>
          <mat-card-actions>
            <button mat-button color="primary">Ver Atletas</button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="dashboard-card">
          <mat-card-header>
            <mat-icon mat-card-avatar>leaderboard</mat-icon>
            <mat-card-title>Leaderboard</mat-card-title>
            <mat-card-subtitle>Veja os rankings</mat-card-subtitle>
          </mat-card-header>
          <mat-card-actions>
            <button mat-button color="primary">Ver Rankings</button>
          </mat-card-actions>
        </mat-card>
      </div>

      <div class="info-section">
        <mat-card>
          <mat-card-content>
            <h3>Sistema funcionando!</h3>
            <p>
              A estrutura Angular foi criada com sucesso e está rodando corretamente.
              Você pode agora desenvolver as funcionalidades específicas do sistema.
            </p>
            <ul>
              <li>✅ Autenticação JWT implementada</li>
              <li>✅ Layout responsivo criado</li>
              <li>✅ Roteamento configurado</li>
              <li>✅ Proxy para API configurado</li>
            </ul>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .welcome-section {
      text-align: center;
      margin-bottom: 40px;
    }

    .welcome-section h1 {
      color: #333;
      margin-bottom: 16px;
    }

    .user-welcome {
      font-size: 18px;
      color: #666;
    }

    .user-role {
      background: #e3f2fd;
      padding: 4px 8px;
      border-radius: 4px;
      color: #1976d2;
      font-weight: 500;
    }

    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 20px;
      margin-bottom: 40px;
    }

    .dashboard-card {
      transition: transform 0.2s ease-in-out;
    }

    .dashboard-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    .info-section {
      margin-top: 40px;
    }

    .info-section ul {
      margin: 16px 0;
    }

    .info-section li {
      margin: 8px 0;
      font-size: 14px;
    }

    mat-card-header mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      color: #1976d2;
    }

    @media (max-width: 600px) {
      .dashboard-container {
        padding: 16px;
      }
      
      .cards-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DashboardComponent {
  currentUser: UsuarioResponseDTO | null = null;

  constructor(private authService: AuthService) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }
}
