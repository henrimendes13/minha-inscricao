import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardEstatisticas, UsuarioResponseDTO } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    RouterModule
  ],
  template: `
    <div class="dashboard-container">
      <div class="welcome-section">
        <h1>Dashboard Administrativo</h1>
        <p *ngIf="currentUser" class="user-welcome">
          Bem-vindo, <strong>{{ currentUser.nome }}</strong>!
          <span class="user-role">{{ currentUser.tipoUsuario }}</span>
        </p>
      </div>

      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Carregando estatísticas...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error && !isLoading" class="error-container">
        <mat-card>
          <mat-card-content>
            <mat-icon color="warn">error</mat-icon>
            <p>{{ error }}</p>
            <button mat-raised-button color="primary" (click)="carregarEstatisticas()">
              Tentar Novamente
            </button>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Dashboard Content -->
      <div *ngIf="estatisticas && !isLoading" class="dashboard-content">

        <!-- Cards de Resumo -->
        <div class="cards-grid">
          <!-- Card Eventos -->
          <mat-card class="stat-card eventos-card">
            <mat-card-header>
              <mat-icon mat-card-avatar>event</mat-icon>
              <mat-card-title>Eventos</mat-card-title>
              <mat-card-subtitle>Total no sistema</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="stat-number">{{ estatisticas.eventos.total }}</div>
              <div class="stat-details">
                <div class="stat-item">
                  <span class="stat-label">Abertos:</span>
                  <span class="stat-value">{{ estatisticas.eventos.inscricoesAbertas }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Em Andamento:</span>
                  <span class="stat-value">{{ estatisticas.eventos.emAndamento }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Concluídos:</span>
                  <span class="stat-value">{{ estatisticas.eventos.concluido }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Card Inscrições -->
          <mat-card class="stat-card inscricoes-card">
            <mat-card-header>
              <mat-icon mat-card-avatar>assignment</mat-icon>
              <mat-card-title>Inscrições</mat-card-title>
              <mat-card-subtitle>Total no sistema</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="stat-number">{{ estatisticas.inscricoes.total }}</div>
              <div class="stat-details">
                <div class="stat-item">
                  <span class="stat-label">Confirmadas:</span>
                  <span class="stat-value success">{{ estatisticas.inscricoes.confirmada }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Pendentes:</span>
                  <span class="stat-value warning">{{ estatisticas.inscricoes.aguardandoPagamento }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Canceladas:</span>
                  <span class="stat-value error">{{ estatisticas.inscricoes.cancelada }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Card Usuários -->
          <mat-card class="stat-card usuarios-card">
            <mat-card-header>
              <mat-icon mat-card-avatar>group</mat-icon>
              <mat-card-title>Usuários</mat-card-title>
              <mat-card-subtitle>Total no sistema</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="stat-number">{{ estatisticas.usuarios.total }}</div>
              <div class="stat-details">
                <div class="stat-item">
                  <span class="stat-label">Atletas:</span>
                  <span class="stat-value">{{ estatisticas.usuarios.atletas }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Organizadores:</span>
                  <span class="stat-value">{{ estatisticas.usuarios.organizadores }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Ativos:</span>
                  <span class="stat-value success">{{ estatisticas.usuarios.ativos }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Card Financeiro -->
          <mat-card class="stat-card financeiro-card">
            <mat-card-header>
              <mat-icon mat-card-avatar>attach_money</mat-icon>
              <mat-card-title>Financeiro</mat-card-title>
              <mat-card-subtitle>Receita simulada</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="stat-number">{{ estatisticas.financeiro.receitaTotal | currency:'BRL' }}</div>
              <div class="stat-details">
                <div class="stat-item">
                  <span class="stat-label">Inscrições Pagas:</span>
                  <span class="stat-value success">{{ estatisticas.financeiro.inscricoesPagas }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Receita Pendente:</span>
                  <span class="stat-value warning">{{ estatisticas.financeiro.receitaPendente | currency:'BRL' }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Eventos Mais Populares -->
        <mat-card class="full-width-card" *ngIf="estatisticas.eventosMaisPopulares.length > 0">
          <mat-card-header>
            <mat-icon mat-card-avatar>trending_up</mat-icon>
            <mat-card-title>Top 5 Eventos Mais Populares</mat-card-title>
            <mat-card-subtitle>Por número de inscrições</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <div class="eventos-populares-list">
              <div *ngFor="let evento of estatisticas.eventosMaisPopulares; let i = index" class="evento-popular-item">
                <span class="evento-rank">{{ i + 1 }}º</span>
                <span class="evento-nome">{{ evento.nome }}</span>
                <span class="evento-inscricoes">{{ evento.totalInscricoes }} inscrições</span>
                <span class="evento-status" [class]="'status-' + evento.status.toLowerCase()">{{ formatStatus(evento.status) }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Gráfico de Inscrições por Mês (Simplificado) -->
        <mat-card class="full-width-card">
          <mat-card-header>
            <mat-icon mat-card-avatar>show_chart</mat-icon>
            <mat-card-title>Inscrições nos Últimos 12 Meses</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="inscricoes-mes-list">
              <div *ngFor="let mes of estatisticas.inscricoesPorMes" class="inscricoes-mes-item">
                <span class="mes-nome">{{ mes.mesNome }}</span>
                <div class="mes-bar-container">
                  <div class="mes-bar" [style.width.%]="calcularPorcentagem(mes.quantidade)"></div>
                </div>
                <span class="mes-quantidade">{{ mes.quantidade }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .welcome-section {
      text-align: center;
      margin-bottom: 40px;
    }

    .welcome-section h1 {
      color: #333;
      margin-bottom: 16px;
      font-size: 32px;
    }

    .user-welcome {
      font-size: 18px;
      color: #666;
    }

    .user-role {
      background: #e3f2fd;
      padding: 4px 12px;
      border-radius: 4px;
      color: #1976d2;
      font-weight: 500;
      margin-left: 8px;
    }

    .loading-container, .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      gap: 20px;
    }

    .error-container mat-card {
      text-align: center;
      padding: 20px;
    }

    .error-container mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 20px;
    }

    .stat-card {
      transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
    }

    .stat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }

    .stat-card mat-icon[mat-card-avatar] {
      font-size: 40px;
      width: 40px;
      height: 40px;
    }

    .eventos-card mat-icon { color: #1976d2; }
    .inscricoes-card mat-icon { color: #388e3c; }
    .usuarios-card mat-icon { color: #f57c00; }
    .financeiro-card mat-icon { color: #7b1fa2; }

    .stat-number {
      font-size: 48px;
      font-weight: bold;
      color: #333;
      margin: 16px 0;
      text-align: center;
    }

    .stat-details {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-top: 16px;
    }

    .stat-item {
      display: flex;
      justify-content: space-between;
      padding: 4px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .stat-label {
      color: #666;
      font-size: 14px;
    }

    .stat-value {
      font-weight: 600;
      color: #333;
    }

    .stat-value.success { color: #388e3c; }
    .stat-value.warning { color: #f57c00; }
    .stat-value.error { color: #d32f2f; }

    .full-width-card {
      width: 100%;
    }

    .eventos-populares-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 16px;
    }

    .evento-popular-item {
      display: grid;
      grid-template-columns: 40px 1fr auto auto;
      gap: 16px;
      padding: 12px;
      background: #f9f9f9;
      border-radius: 8px;
      align-items: center;
    }

    .evento-rank {
      font-weight: bold;
      font-size: 20px;
      color: #1976d2;
      text-align: center;
    }

    .evento-nome {
      font-weight: 500;
      color: #333;
    }

    .evento-inscricoes {
      font-weight: 600;
      color: #388e3c;
    }

    .evento-status {
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      text-transform: uppercase;
    }

    .status-inscricoes_abertas { background: #c8e6c9; color: #2e7d32; }
    .status-em_andamento { background: #fff9c4; color: #f57f17; }
    .status-concluido { background: #e0e0e0; color: #424242; }

    .inscricoes-mes-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 16px;
    }

    .inscricoes-mes-item {
      display: grid;
      grid-template-columns: 150px 1fr 60px;
      gap: 12px;
      align-items: center;
    }

    .mes-nome {
      font-size: 14px;
      color: #666;
    }

    .mes-bar-container {
      background: #e0e0e0;
      border-radius: 4px;
      height: 24px;
      overflow: hidden;
    }

    .mes-bar {
      background: linear-gradient(90deg, #1976d2, #42a5f5);
      height: 100%;
      border-radius: 4px;
      transition: width 0.3s ease;
    }

    .mes-quantidade {
      font-weight: 600;
      color: #333;
      text-align: right;
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 16px;
      }

      .cards-grid {
        grid-template-columns: 1fr;
      }

      .evento-popular-item {
        grid-template-columns: 30px 1fr;
        gap: 8px;
      }

      .evento-inscricoes,
      .evento-status {
        grid-column: 2;
      }

      .inscricoes-mes-item {
        grid-template-columns: 100px 1fr 50px;
        gap: 8px;
      }
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: UsuarioResponseDTO | null = null;
  estatisticas: DashboardEstatisticas | null = null;
  isLoading = false;
  error: string | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService
  ) {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
      });
  }

  ngOnInit(): void {
    this.carregarEstatisticas();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  carregarEstatisticas(): void {
    this.isLoading = true;
    this.error = null;

    this.dashboardService.obterEstatisticas()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.estatisticas = data;
          this.isLoading = false;
          console.log('Estatísticas carregadas:', data);
        },
        error: (err) => {
          console.error('Erro ao carregar estatísticas:', err);
          this.error = 'Erro ao carregar estatísticas. Você pode não ter permissão de acesso.';
          this.isLoading = false;
        }
      });
  }

  calcularPorcentagem(quantidade: number): number {
    if (!this.estatisticas?.inscricoesPorMes) return 0;

    const maxQuantidade = Math.max(...this.estatisticas.inscricoesPorMes.map(m => m.quantidade), 1);
    return (quantidade / maxQuantidade) * 100;
  }

  formatStatus(status: string): string {
    const statusMap: { [key: string]: string } = {
      'RASCUNHO': 'Rascunho',
      'PUBLICADO': 'Publicado',
      'INSCRICOES_ABERTAS': 'Abertas',
      'INSCRICOES_ENCERRADAS': 'Encerradas',
      'EM_ANDAMENTO': 'Em Andamento',
      'CONCLUIDO': 'Concluído',
      'CANCELADO': 'Cancelado'
    };
    return statusMap[status] || status;
  }
}
