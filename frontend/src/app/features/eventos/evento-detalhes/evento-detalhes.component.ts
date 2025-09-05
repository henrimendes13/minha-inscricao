import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';

import { EventoService } from '../../../core/services/evento.service';
import { EventoApiResponse } from '../../../models/evento.model';

@Component({
  selector: 'app-evento-detalhes',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDividerModule,
    MatChipsModule
  ],
  template: `
    <div class="evento-detalhes-container">
      <!-- Loading State -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner></mat-spinner>
        <p>Carregando detalhes do evento...</p>
      </div>

      <!-- Error State -->
      <div class="error-container" *ngIf="hasError && !isLoading">
        <mat-icon>error_outline</mat-icon>
        <h3>Erro ao carregar evento</h3>
        <p>Não foi possível encontrar os detalhes deste evento.</p>
        <button mat-raised-button color="primary" (click)="voltar()">
          <mat-icon>arrow_back</mat-icon>
          Voltar para lista
        </button>
      </div>

      <!-- Event Details -->
      <div class="evento-content" *ngIf="evento && !isLoading && !hasError">
        <!-- Header -->
        <div class="header-section">
          <button mat-icon-button (click)="voltar()" class="back-button">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="header-info">
            <h1>{{ evento.nome }}</h1>
            <div class="organizer-info">
              <mat-icon>person</mat-icon>
              <span>Organizado por {{ evento.nomeOrganizador }}</span>
            </div>
          </div>
        </div>

        <!-- Main Content Grid -->
        <div class="content-grid">
          <!-- Left Column - Event Image & Info -->
          <div class="left-column">
            <div class="event-image">
              <img [src]="getEventoImage(evento)" [alt]="evento.nome" />
              <div class="image-overlay">
                <mat-chip [style.background-color]="getStatusColor(evento.status)" class="status-chip">
                  {{ evento.descricaoStatus }}
                </mat-chip>
              </div>
            </div>

            <mat-card class="event-info-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon>info</mat-icon>
                  Informações do Evento
                </mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="info-item">
                  <mat-icon>calendar_today</mat-icon>
                  <div class="info-content">
                    <strong>Período:</strong>
                    <span>{{ formatarDataRange(evento.dataInicioDoEvento, evento.dataFimDoEvento) }}</span>
                  </div>
                </div>
                
                <div class="info-item">
                  <mat-icon>category</mat-icon>
                  <div class="info-content">
                    <strong>Categorias:</strong>
                    <span>{{ evento.totalCategorias }} disponíveis</span>
                  </div>
                </div>

                <div class="info-item">
                  <mat-icon>people</mat-icon>
                  <div class="info-content">
                    <strong>Inscrições:</strong>
                    <span>{{ evento.inscricoesAtivas }} ativas</span>
                  </div>
                </div>

                <div class="info-item">
                  <mat-icon>schedule</mat-icon>
                  <div class="info-content">
                    <strong>Criado em:</strong>
                    <span>{{ formatarData(evento.createdAt) }}</span>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>

          <!-- Right Column - Actions & Details -->
          <div class="right-column">
            <mat-card class="action-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon>sports</mat-icon>
                  Participar do Evento
                </mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="action-info">
                  <div class="price-section" *ngIf="evento.podeReceberInscricoes">
                    <div class="price-label">Valor da Inscrição</div>
                    <div class="price-value">Gratuito</div>
                  </div>
                  
                  <div class="status-section">
                    <div class="status-indicator" [class]="getStatusClass(evento.status)">
                      <mat-icon>{{ getStatusIcon(evento.status) }}</mat-icon>
                      {{ evento.descricaoStatus }}
                    </div>
                  </div>

                  <mat-divider></mat-divider>

                  <div class="action-buttons">
                    <button 
                      mat-raised-button 
                      color="primary" 
                      class="inscricao-button"
                      [disabled]="!evento.podeReceberInscricoes"
                      (click)="fazerInscricao()">
                      <mat-icon>{{ evento.podeReceberInscricoes ? 'add' : 'block' }}</mat-icon>
                      {{ evento.podeReceberInscricoes ? 'Fazer Inscrição' : 'Inscrições Fechadas' }}
                    </button>

                    <button mat-button class="share-button" (click)="compartilhar()">
                      <mat-icon>share</mat-icon>
                      Compartilhar
                    </button>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>

            <mat-card class="details-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon>description</mat-icon>
                  Detalhes Adicionais
                </mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="detail-item">
                  <strong>ID do Evento:</strong> #{{ evento.id }}
                </div>
                <div class="detail-item">
                  <strong>Status:</strong> {{ evento.status }}
                </div>
                <div class="detail-item">
                  <strong>Permite Inscrições:</strong> 
                  <span class="boolean-value" [class.active]="evento.podeReceberInscricoes">
                    {{ evento.podeReceberInscricoes ? 'Sim' : 'Não' }}
                  </span>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrl: './evento-detalhes.component.scss'
})
export class EventoDetalhesComponent implements OnInit {
  evento: EventoApiResponse | null = null;
  isLoading = false;
  hasError = false;
  eventoId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventoService: EventoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.eventoId = +params['id'];
      if (this.eventoId) {
        this.carregarEvento();
      } else {
        this.hasError = true;
      }
    });
  }

  carregarEvento(): void {
    this.isLoading = true;
    this.hasError = false;

    this.eventoService.buscarEventoPorId(this.eventoId).subscribe({
      next: (evento) => {
        this.evento = evento;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.hasError = true;
        console.error('Erro ao carregar evento:', error);
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/eventos']);
  }

  fazerInscricao(): void {
    if (!this.evento || !this.evento.podeReceberInscricoes) {
      return;
    }

    this.snackBar.open('Funcionalidade de inscrição em desenvolvimento', 'Fechar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  compartilhar(): void {
    if (navigator.share && this.evento) {
      navigator.share({
        title: this.evento.nome,
        text: `Confira este evento: ${this.evento.nome}`,
        url: window.location.href
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      this.snackBar.open('Link copiado para a área de transferência', 'Fechar', {
        duration: 2000
      });
    }
  }

  getEventoImage(evento: EventoApiResponse): string {
    const imageHashes = [
      'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=600&h=300&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=600&h=300&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600&h=300&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600&h=300&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=600&h=300&fit=crop&crop=center'
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

  formatarData(dataIso: string): string {
    const data = new Date(dataIso);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ABERTO':
        return '#4caf50';
      case 'RASCUNHO':
        return '#ff9800';
      case 'FECHADO':
        return '#f44336';
      default:
        return '#607d8b';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ABERTO':
        return 'status-open';
      case 'RASCUNHO':
        return 'status-draft';
      case 'FECHADO':
        return 'status-closed';
      default:
        return 'status-default';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'ABERTO':
        return 'check_circle';
      case 'RASCUNHO':
        return 'schedule';
      case 'FECHADO':
        return 'cancel';
      default:
        return 'help';
    }
  }
}