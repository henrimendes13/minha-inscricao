import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';

import { AnexoService } from '../../../core/services/anexo.service';
import { EventoService } from '../../../core/services/evento.service';
import { LeaderboardService } from '../../../core/services/leaderboard.service';
import { TimelineService } from '../../../core/services/timeline.service';
import { WorkoutService } from '../../../core/services/workout.service';

import { AnexoResponse } from '../../../models/anexo.model';
import { EventoApiResponse } from '../../../models/evento.model';
import { Categoria, LeaderboardResponse } from '../../../models/leaderboard.model';
import { TimelineResponse } from '../../../models/timeline.model';
import { WorkoutResponse } from '../../../models/workout.model';

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
    MatChipsModule,
    MatTabsModule,
    MatBadgeModule
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

      <!-- Event Details with Tabs -->
      <div class="evento-content" *ngIf="evento && !isLoading && !hasError">
        <!-- Header (sempre visível) -->
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

        <!-- Abas Dinâmicas -->
        <div class="tabs-container">
          <mat-tab-group [(selectedIndex)]="selectedTabIndex" (selectedTabChange)="onTabChange($event)">
            
            <!-- Aba Evento -->
            <mat-tab>
              <ng-template mat-tab-label>
                <mat-icon>event</mat-icon>
                <span>Evento</span>
              </ng-template>
              <div class="tab-content">
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
            </mat-tab>

            <!-- Aba Timeline -->
            <mat-tab>
              <ng-template mat-tab-label>
                <mat-icon>timeline</mat-icon>
                <span>Timeline</span>
              </ng-template>
              <div class="tab-content">
                <div *ngIf="timelineLoading" class="tab-loading">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando timeline...</p>
                </div>
                <div *ngIf="timelineError && !timelineLoading" class="tab-error">
                  <mat-icon>error_outline</mat-icon>
                  <p>Erro ao carregar timeline</p>
                </div>
                <div *ngIf="timelineData && !timelineLoading && !timelineError" class="timeline-content">
                  <div *ngIf="timelineData.items.length === 0" class="empty-state">
                    <mat-icon>timeline</mat-icon>
                    <p>Nenhum item na timeline ainda</p>
                  </div>
                  <div *ngFor="let item of timelineData.items" class="timeline-item">
                    <div class="timeline-marker">
                      <mat-icon>{{ getTimelineIcon(item.tipo) }}</mat-icon>
                    </div>
                    <div class="timeline-content-item">
                      <h4>{{ item.titulo }}</h4>
                      <p>{{ item.descricao }}</p>
                      <small>{{ formatarData(item.dataHora) }}</small>
                    </div>
                  </div>
                </div>
              </div>
            </mat-tab>

            <!-- Aba Leaderboard -->
            <mat-tab>
              <ng-template mat-tab-label>
                <mat-icon>leaderboard</mat-icon>
                <span>Leaderboard</span>
              </ng-template>
              <div class="tab-content">
                <div *ngIf="leaderboardLoading" class="tab-loading">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando leaderboard...</p>
                </div>
                <div *ngIf="leaderboardError && !leaderboardLoading" class="tab-error">
                  <mat-icon>error_outline</mat-icon>
                  <p>Erro ao carregar leaderboard</p>
                </div>
                <div *ngIf="leaderboardData && !leaderboardLoading && !leaderboardError" class="leaderboard-content">
                  <div *ngIf="leaderboardData.entries.length === 0" class="empty-state">
                    <mat-icon>leaderboard</mat-icon>
                    <p>Nenhuma classificação disponível ainda</p>
                  </div>
                  <div *ngFor="let entry of leaderboardData.entries; let i = index" class="leaderboard-entry">
                    <div class="position">{{ entry.posicao }}º</div>
                    <div class="name">{{ entry.nome }}</div>
                    <div class="score">{{ entry.pontuacao }} pts</div>
                    <div *ngIf="entry.tempo" class="time">{{ entry.tempo }}</div>
                  </div>
                </div>
              </div>
            </mat-tab>

            <!-- Aba Workouts -->
            <mat-tab>
              <ng-template mat-tab-label>
                <mat-icon>fitness_center</mat-icon>
                <span>Workouts</span>
              </ng-template>
              <div class="tab-content">
                <div *ngIf="workoutLoading" class="tab-loading">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando workouts...</p>
                </div>
                <div *ngIf="workoutError && !workoutLoading" class="tab-error">
                  <mat-icon>error_outline</mat-icon>
                  <p>Erro ao carregar workouts</p>
                </div>
                <div *ngIf="workoutData && !workoutLoading && !workoutError" class="workout-content">
                  <div *ngIf="workoutData.workouts.length === 0" class="empty-state">
                    <mat-icon>fitness_center</mat-icon>
                    <p>Nenhum workout cadastrado ainda</p>
                  </div>
                  <mat-card *ngFor="let workout of workoutData.workouts" class="workout-card">
                    <mat-card-header>
                      <mat-card-title>{{ workout.nome }}</mat-card-title>
                      <mat-card-subtitle>{{ workout.tipo }}</mat-card-subtitle>
                    </mat-card-header>
                    <mat-card-content>
                      <p *ngIf="workout.descricao">{{ workout.descricao }}</p>
                      <div *ngIf="workout.instrucoes" class="workout-instructions">
                        <strong>Instruções:</strong>
                        <p>{{ workout.instrucoes }}</p>
                      </div>
                      <div *ngIf="workout.equipamentos?.length" class="workout-equipment">
                        <strong>Equipamentos:</strong>
                        <mat-chip-listbox>
                          <mat-chip *ngFor="let equip of workout.equipamentos">{{ equip }}</mat-chip>
                        </mat-chip-listbox>
                      </div>
                    </mat-card-content>
                  </mat-card>
                </div>
              </div>
            </mat-tab>

            <!-- Aba Documentos -->
            <mat-tab>
              <ng-template mat-tab-label>
                <mat-icon>folder</mat-icon>
                <span>Documentos</span>
              </ng-template>
              <div class="tab-content">
                <div *ngIf="anexoLoading" class="tab-loading">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando documentos...</p>
                </div>
                <div *ngIf="anexoError && !anexoLoading" class="tab-error">
                  <mat-icon>error_outline</mat-icon>
                  <p>Erro ao carregar documentos</p>
                </div>
                <div *ngIf="anexoData && !anexoLoading && !anexoError" class="anexo-content">
                  <div *ngIf="anexoData.anexos.length === 0" class="empty-state">
                    <mat-icon>folder</mat-icon>
                    <p>Nenhum documento disponível</p>
                  </div>
                  <div *ngFor="let anexo of anexoData.anexos" class="anexo-item">
                    <mat-icon>{{ getAnexoIcon(anexo.tipo) }}</mat-icon>
                    <div class="anexo-info">
                      <h4>{{ anexo.nome }}</h4>
                      <p *ngIf="anexo.descricao">{{ anexo.descricao }}</p>
                      <small>{{ formatarTamanho(anexo.tamanho) }} • {{ anexo.extensao.toUpperCase() }}</small>
                    </div>
                    <button mat-icon-button (click)="downloadAnexo(anexo.id)">
                      <mat-icon>download</mat-icon>
                    </button>
                  </div>
                </div>
              </div>
            </mat-tab>

          </mat-tab-group>
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
  selectedTabIndex = 0;

  // Timeline
  timelineData: TimelineResponse | null = null;
  timelineLoading = false;
  timelineError = false;

  // Leaderboard
  leaderboardData: LeaderboardResponse | null = null;
  leaderboardLoading = false;
  leaderboardError = false;
  categorias: Categoria[] = [];

  // Workouts
  workoutData: WorkoutResponse | null = null;
  workoutLoading = false;
  workoutError = false;

  // Anexos
  anexoData: AnexoResponse | null = null;
  anexoLoading = false;
  anexoError = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventoService: EventoService,
    private timelineService: TimelineService,
    private leaderboardService: LeaderboardService,
    private workoutService: WorkoutService,
    private anexoService: AnexoService,
    private snackBar: MatSnackBar
  ) { }

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

  onTabChange(event: any): void {
    const tabIndex = event.index;

    switch (tabIndex) {
      case 1: // Timeline
        this.carregarTimeline();
        break;
      case 2: // Leaderboard
        this.carregarLeaderboard();
        break;
      case 3: // Workouts
        this.carregarWorkouts();
        break;
      case 4: // Documentos
        this.carregarAnexos();
        break;
    }
  }

  carregarTimeline(): void {
    if (this.timelineData) return; // Cache

    this.timelineLoading = true;
    this.timelineError = false;

    this.timelineService.buscarTimelinePorEvento(this.eventoId).subscribe({
      next: (data) => {
        this.timelineData = data;
        this.timelineLoading = false;
      },
      error: (error) => {
        this.timelineLoading = false;
        this.timelineError = true;
        console.error('Erro ao carregar timeline:', error);
      }
    });
  }

  carregarLeaderboard(): void {
    if (this.leaderboardData) return; // Cache

    // Primeiro carrega categorias, depois o leaderboard da primeira categoria
    this.leaderboardLoading = true;
    this.leaderboardError = false;

    this.leaderboardService.buscarCategoriasPorEvento(this.eventoId).subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        if (categorias.length > 0) {
          this.leaderboardService.buscarLeaderboardPorEventoECategoria(this.eventoId, categorias[0].id).subscribe({
            next: (data) => {
              this.leaderboardData = data;
              this.leaderboardLoading = false;
            },
            error: (error) => {
              this.leaderboardLoading = false;
              this.leaderboardError = true;
              console.error('Erro ao carregar leaderboard:', error);
            }
          });
        } else {
          this.leaderboardLoading = false;
          this.leaderboardData = { entries: [], categoria: '', totalParticipantes: 0 };
        }
      },
      error: (error) => {
        this.leaderboardLoading = false;
        this.leaderboardError = true;
        console.error('Erro ao carregar categorias:', error);
      }
    });
  }

  carregarWorkouts(): void {
    if (this.workoutData) return; // Cache

    this.workoutLoading = true;
    this.workoutError = false;

    this.workoutService.buscarWorkoutsPorEvento(this.eventoId).subscribe({
      next: (data) => {
        this.workoutData = data;
        this.workoutLoading = false;
      },
      error: (error) => {
        this.workoutLoading = false;
        this.workoutError = true;
        console.error('Erro ao carregar workouts:', error);
      }
    });
  }

  carregarAnexos(): void {
    if (this.anexoData) return; // Cache

    this.anexoLoading = true;
    this.anexoError = false;

    this.anexoService.buscarAnexosPorEvento(this.eventoId).subscribe({
      next: (data) => {
        this.anexoData = data;
        this.anexoLoading = false;
      },
      error: (error) => {
        this.anexoLoading = false;
        this.anexoError = true;
        console.error('Erro ao carregar anexos:', error);
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

  downloadAnexo(anexoId: number): void {
    this.anexoService.downloadAnexo(anexoId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'documento';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (error) => {
        this.snackBar.open('Erro ao fazer download do documento', 'Fechar', {
          duration: 3000
        });
      }
    });
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

  formatarTamanho(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
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

  getTimelineIcon(tipo: string): string {
    switch (tipo) {
      case 'INSCRICOES_ABERTAS':
        return 'how_to_reg';
      case 'INSCRICOES_FECHADAS':
        return 'person_off';
      case 'INICIO_EVENTO':
        return 'play_arrow';
      case 'FIM_EVENTO':
        return 'stop';
      case 'DIVULGACAO_RESULTADO':
        return 'emoji_events';
      default:
        return 'event';
    }
  }

  getAnexoIcon(tipo: string): string {
    switch (tipo) {
      case 'REGULAMENTO':
        return 'gavel';
      case 'EDITAL':
        return 'description';
      case 'CRONOGRAMA':
        return 'schedule';
      case 'MAPA':
        return 'map';
      case 'FOTO':
        return 'photo';
      case 'VIDEO':
        return 'videocam';
      case 'DOCUMENTO':
        return 'insert_drive_file';
      default:
        return 'attachment';
    }
  }
}