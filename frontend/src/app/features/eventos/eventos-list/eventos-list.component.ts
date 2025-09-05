import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { EventoService } from '../../../core/services/evento.service';
import { EventoApiResponse } from '../../../models/evento.model';

@Component({
  selector: 'app-eventos-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="eventos-container">
      <div class="header-section">
        <h1>Eventos Esportivos</h1>
        <p class="subtitle">Descubra e participe dos melhores eventos esportivos</p>
      </div>

      <!-- Loading Spinner -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner></mat-spinner>
        <p>Carregando eventos...</p>
      </div>

      <!-- Error State -->
      <div class="error-container" *ngIf="hasError && !isLoading">
        <div class="error-card">
          <mat-icon>error_outline</mat-icon>
          <h3>Erro ao carregar eventos</h3>
          <p>Não foi possível conectar com a API. Verifique se o backend está rodando.</p>
          <button mat-raised-button color="primary" (click)="carregarEventos()">
            <mat-icon>refresh</mat-icon>
            Tentar novamente
          </button>
        </div>
      </div>
      
      <!-- Lista de Eventos -->
      <div class="eventos-grid" *ngIf="!isLoading && !hasError && eventos.length > 0">
        <div class="evento-card" *ngFor="let evento of eventos" (click)="verDetalhes(evento.id)">
          <div class="evento-image">
            <img [src]="getEventoImage(evento)" [alt]="evento.nome" />
            <div class="image-overlay"></div>
          </div>
          
          <div class="evento-content">
            <h3 class="evento-title">{{ evento.nome }}</h3>
            
            <div class="evento-dates">
              <div class="date-item">
                <mat-icon>calendar_today</mat-icon>
                <span>{{ formatarDataRange(evento.dataInicioDoEvento, evento.dataFimDoEvento) }}</span>
              </div>
            </div>
            
            <div class="evento-actions">
              <button mat-raised-button color="primary" class="ver-detalhes-btn">
                Ver Detalhes
                <mat-icon>arrow_forward</mat-icon>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="!isLoading && !hasError && eventos.length === 0">
        <mat-icon>event_busy</mat-icon>
        <h3>Nenhum evento encontrado</h3>
        <p>Não há eventos disponíveis no momento.</p>
      </div>
    </div>
  `,
  styles: [`
    .eventos-container {
      min-height: 100vh;
      background: var(--bg-primary);
      padding: 40px 20px;
    }

    .header-section {
      text-align: center;
      margin-bottom: 60px;
      max-width: 600px;
      margin-left: auto;
      margin-right: auto;
    }

    .header-section h1 {
      font-size: 3rem;
      font-weight: 700;
      background: linear-gradient(135deg, var(--accent-primary) 0%, #26c6da 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 16px;
      letter-spacing: -0.02em;
    }

    .subtitle {
      font-size: 1.2rem;
      color: var(--text-secondary);
      margin: 0;
      font-weight: 400;
    }

    .eventos-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 32px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .evento-card {
      background: var(--bg-card);
      border-radius: 20px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid var(--border-color);
      box-shadow: var(--shadow-md);
      position: relative;
    }

    .evento-card:hover {
      transform: translateY(-8px) scale(1.02);
      box-shadow: var(--shadow-lg);
      border-color: var(--accent-primary);
    }

    .evento-image {
      position: relative;
      height: 220px;
      overflow: hidden;
    }

    .evento-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s ease;
    }

    .evento-card:hover .evento-image img {
      transform: scale(1.1);
    }

    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
    }

    .evento-content {
      padding: 24px;
    }

    .evento-title {
      font-size: 1.5rem;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 20px 0;
      line-height: 1.3;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .evento-dates {
      margin-bottom: 24px;
    }

    .date-item {
      display: flex;
      align-items: center;
      gap: 12px;
      color: var(--text-secondary);
      font-size: 0.95rem;
      font-weight: 500;
    }

    .date-item mat-icon {
      color: var(--accent-primary);
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .evento-actions {
      display: flex;
      justify-content: flex-end;
    }

    .ver-detalhes-btn {
      background: linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-hover) 100%) !important;
      color: white !important;
      border: none !important;
      padding: 12px 24px !important;
      border-radius: 12px !important;
      font-weight: 600 !important;
      font-size: 0.9rem !important;
      transition: all 0.2s ease !important;
      display: flex !important;
      align-items: center !important;
      gap: 8px !important;
    }

    .ver-detalhes-btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0, 188, 212, 0.3);
    }

    .ver-detalhes-btn mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 80px 20px;
      gap: 20px;
    }

    .loading-container mat-spinner {
      --mdc-circular-progress-active-indicator-color: var(--accent-primary);
    }

    .loading-container p {
      color: var(--text-secondary);
      font-size: 1.1rem;
      margin: 0;
    }

    .error-container {
      display: flex;
      justify-content: center;
      padding: 60px 20px;
    }

    .error-card {
      background: var(--bg-card);
      border-radius: 16px;
      padding: 48px;
      text-align: center;
      border: 1px solid rgba(244, 67, 54, 0.2);
      max-width: 400px;
    }

    .error-card mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #f44336;
      margin-bottom: 16px;
    }

    .error-card h3 {
      color: var(--text-primary);
      margin: 0 0 12px 0;
      font-size: 1.3rem;
      font-weight: 600;
    }

    .error-card p {
      color: var(--text-secondary);
      margin: 0 0 24px 0;
      line-height: 1.5;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 80px 20px;
      text-align: center;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: var(--text-tertiary);
      margin-bottom: 24px;
    }

    .empty-state h3 {
      color: var(--text-primary);
      margin: 0 0 12px 0;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .empty-state p {
      color: var(--text-secondary);
      margin: 0;
      font-size: 1.1rem;
    }

    @media (max-width: 768px) {
      .eventos-container {
        padding: 20px 16px;
      }

      .header-section {
        margin-bottom: 40px;
      }

      .header-section h1 {
        font-size: 2.2rem;
      }

      .subtitle {
        font-size: 1.1rem;
      }

      .eventos-grid {
        grid-template-columns: 1fr;
        gap: 24px;
      }

      .evento-card {
        border-radius: 16px;
      }

      .evento-image {
        height: 200px;
      }

      .evento-content {
        padding: 20px;
      }

      .evento-title {
        font-size: 1.3rem;
      }
    }

    @media (max-width: 480px) {
      .header-section h1 {
        font-size: 1.8rem;
      }

      .subtitle {
        font-size: 1rem;
      }

      .evento-image {
        height: 180px;
      }

      .evento-content {
        padding: 16px;
      }
    }
  `]
})
export class EventosListComponent implements OnInit {
  eventos: EventoApiResponse[] = [];
  isLoading = false;
  hasError = false;

  constructor(
    public eventoService: EventoService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregarEventos();
  }

  carregarEventos(): void {
    this.isLoading = true;
    this.hasError = false;

    this.eventoService.listarEventos().subscribe({
      next: (eventos) => {
        this.eventos = eventos;
        this.isLoading = false;
        console.log('Eventos carregados:', eventos);
      },
      error: (error) => {
        this.isLoading = false;
        this.hasError = true;
        console.error('Erro ao carregar eventos:', error);
        
        this.snackBar.open('Erro ao carregar eventos da API', 'Fechar', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  verDetalhes(eventoId: number): void {
    this.router.navigate(['/eventos', eventoId]);
  }

  getEventoImage(evento: EventoApiResponse): string {
    // Por enquanto, usar imagens placeholder baseadas no nome do evento
    const imageHashes = [
      'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=400&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=800&h=400&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=400&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800&h=400&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=800&h=400&fit=crop&crop=center'
    ];
    
    return imageHashes[evento.id % imageHashes.length];
  }

  formatarDataRange(dataInicio: string, dataFim: string): string {
    const inicio = new Date(dataInicio);
    const fim = new Date(dataFim);
    
    const formatarData = (data: Date) => {
      return data.toLocaleDateString('pt-BR', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      });
    };

    if (inicio.toDateString() === fim.toDateString()) {
      return formatarData(inicio);
    } else {
      return `${formatarData(inicio)} - ${formatarData(fim)}`;
    }
  }
}