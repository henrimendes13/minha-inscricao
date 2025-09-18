import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
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

import { AuthHelpers } from '../../../core/auth/auth-helpers';
import { AuthService } from '../../../core/auth/auth.service';
import { AnexoService } from '../../../core/services/anexo.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { EventoService } from '../../../core/services/evento.service';
import { LeaderboardService } from '../../../core/services/leaderboard.service';
import { TimelineService } from '../../../core/services/timeline.service';
import { WorkoutService } from '../../../core/services/workout.service';

import { Anexo, AnexoResponse } from '../../../models/anexo.model';
import { EventoApiResponse } from '../../../models/evento.model';
import { Categoria, CategoriaInscricao, LeaderboardRanking, LeaderboardResponse, WorkoutPosicao } from '../../../models/leaderboard.model';
import { Timeline } from '../../../models/timeline.model';
import { Workout, WorkoutsByCategory } from '../../../models/workout.model';

@Component({
  selector: 'app-evento-detalhes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
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
              <mat-icon>location_on</mat-icon>
              <span>{{ evento.endereco }}, {{ evento.cidade }} - {{ evento.estado }}</span>
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

                    <!-- Seção Sobre o Evento -->
                    <div class="evento-sobre-section" *ngIf="evento.descricao">
                      <h3 class="sobre-titulo">Sobre o Evento:</h3>
                      <p class="evento-descricao">{{ evento.descricao }}</p>
                    </div>
                  </div>

                  <!-- Right Column - Actions & Details -->
                  <div class="right-column">
                    <br>
                    <!-- Seção de Inscrições Disponíveis -->
                    <mat-card class="inscricoes-card" *ngIf="evento.podeReceberInscricoes">
                      <br>
                        <mat-card-title>
                          <mat-icon>sports</mat-icon>
                          Selecione seu ingresso
                        </mat-card-title>
                        <br>
                      <mat-card-content>
                        <!-- Loading State -->
                        <div *ngIf="categoriasCarregando" class="loading-categorias">
                          <mat-spinner diameter="30"></mat-spinner>
                          <span>Carregando categorias...</span>
                        </div>

                        <!-- Error State -->
                        <div *ngIf="categoriasError && !categoriasCarregando" class="error-categorias">
                          <mat-icon>error_outline</mat-icon>
                          <span>Erro ao carregar categorias</span>
                        </div>

                        <!-- Lista de Categorias -->
                        <div *ngIf="!categoriasCarregando && !categoriasError" class="categorias-list">
                          <!-- Empty State -->
                          <div *ngIf="categoriasInscricao.length === 0" class="empty-categorias">
                            <mat-icon>info_outline</mat-icon>
                            <span>Nenhuma categoria disponível para inscrição</span>
                          </div>

                          <!-- Categorias Disponíveis -->
                          <div *ngFor="let categoria of categoriasInscricao" class="categoria-item">
                            <div class="categoria-info">
                              <h3>{{ categoria.nome }}</h3>
                              <p *ngIf="categoria.descricao" class="categoria-descricao">{{ categoria.descricao }}</p>
                              <span class="categoria-valor">R$ {{ categoria.valorInscricao | number:'1.2-2' }} por atleta + taxas</span>
                            </div>
                            
                            <div class="categoria-controles">
                              <button 
                                mat-icon-button 
                                (click)="removerCategoria(categoria.id)" 
                                [disabled]="!getQuantidadeCategoria(categoria.id)"
                                class="controle-btn remove-btn">
                                <mat-icon>remove</mat-icon>
                              </button>
                              
                              <span class="quantidade">{{ getQuantidadeCategoria(categoria.id) }}</span>
                              
                              <button 
                                mat-icon-button 
                                (click)="adicionarCategoria(categoria.id)"
                                class="controle-btn add-btn">
                                <mat-icon>add</mat-icon>
                              </button>
                            </div>
                          </div>

                          <!-- Seção de Comprar -->
                          <div *ngIf="temItensNoCarrinho()" class="comprar-section">
                            <div class="valor-total">
                              <strong>Total: R$ {{ calcularValorTotal() | number:'1.2-2' }}</strong>
                            </div>
                            <button 
                              mat-raised-button 
                              color="primary" 
                              class="comprar-button" 
                              (click)="prosseguirInscricao()">
                              COMPRAR INGRESSO
                            </button>
                          </div>
                        </div>

                        <!-- Botão Compartilhar (sempre visível) -->
                        <div class="share-section">
                          <mat-divider></mat-divider>
                          <button mat-button class="share-button" (click)="compartilhar()">
                            <mat-icon>share</mat-icon>
                            Compartilhar
                          </button>
                        </div>
                      </mat-card-content>
                    </mat-card>

                    <!-- Fallback para eventos sem inscrições abertas -->
                    <mat-card class="action-card" *ngIf="!evento.podeReceberInscricoes">
                      <mat-card-header>
                        <mat-card-title>
                          <mat-icon>sports</mat-icon>
                          Status do Evento
                        </mat-card-title>
                      </mat-card-header>
                      <mat-card-content>
                        <div class="action-info">
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
                              class="inscricao-button"
                              disabled>
                              <mat-icon>block</mat-icon>
                              Inscrições Fechadas
                            </button>

                            <button mat-button class="share-button" (click)="compartilhar()">
                              <mat-icon>share</mat-icon>
                              Compartilhar
                            </button>
                          </div>
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
                <!-- Loading State -->
                <div *ngIf="timelineLoading" class="loading-state small">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando timeline...</p>
                </div>
                
                <!-- Error State -->
                <div *ngIf="timelineError && !timelineLoading" class="error-state small">
                  <mat-icon>error_outline</mat-icon>
                  <h3>Erro ao carregar timeline</h3>
                  <p>Não foi possível carregar a programação do evento.</p>
                </div>
                
                <!-- Empty State -->
                <div *ngIf="timelineData?.vazia && !timelineLoading && !timelineError" class="empty-state small">
                  <mat-icon>info_outline</mat-icon>
                  <h3>Timeline não disponível</h3>
                  <p>A timeline detalhada para este evento ainda não foi publicada.</p>
                </div>

                <!-- Timeline Content -->
                <div *ngIf="timelineData && !timelineData.vazia && !timelineLoading && !timelineError" class="details-grid">
                  <mat-card class="timeline-card" *ngIf="timelineData.temDescricaoDiaUm">
                    <mat-card-header>
                      <mat-card-title>Dia 1</mat-card-title>
                    </mat-card-header>
                    <mat-card-content>
                      <p>{{ timelineData.descricaoDiaUm }}</p>
                    </mat-card-content>
                  </mat-card>

                  <mat-card class="timeline-card" *ngIf="timelineData.temDescricaoDiaDois">
                    <mat-card-header>
                      <mat-card-title>Dia 2</mat-card-title>
                    </mat-card-header>
                    <mat-card-content>
                      <p>{{ timelineData.descricaoDiaDois }}</p>
                    </mat-card-content>
                  </mat-card>

                  <mat-card class="timeline-card" *ngIf="timelineData.temDescricaoDiaTres">
                    <mat-card-header>
                      <mat-card-title>Dia 3</mat-card-title>
                    </mat-card-header>
                    <mat-card-content>
                      <p>{{ timelineData.descricaoDiaTres }}</p>
                    </mat-card-content>
                  </mat-card>

                  <mat-card class="timeline-card" *ngIf="timelineData.temDescricaoDiaQuatro">
                    <mat-card-header>
                      <mat-card-title>Dia 4</mat-card-title>
                    </mat-card-header>
                    <mat-card-content>
                      <p>{{ timelineData.descricaoDiaQuatro }}</p>
                    </mat-card-content>
                  </mat-card>
                </div>
              </div>
            </mat-tab>

            <!-- Aba Leaderboard -->
            <mat-tab>
              <ng-template mat-tab-label>
                <mat-icon>leaderboard</mat-icon>
                <span>Leaderboard</span>
              </ng-template>
              <div class="tab-content leaderboard-tab">
                <!-- Loading State -->
                <div *ngIf="leaderboardLoading" class="tab-loading">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando leaderboard...</p>
                </div>

                <!-- Error State -->
                <div *ngIf="leaderboardError && !leaderboardLoading" class="tab-error">
                  <mat-icon>error_outline</mat-icon>
                  <p>Erro ao carregar leaderboard</p>
                </div>

                <!-- Leaderboard Content -->
                <div *ngIf="!leaderboardLoading && !leaderboardError" class="leaderboard-content">
                  
                  <!-- Filtros e Ações -->
                  <div class="leaderboard-header" *ngIf="categorias.length > 0">
                    <div class="filter-group">
                      <label for="categoria-select">Categoria:</label>
                      <select 
                        id="categoria-select" 
                        [(ngModel)]="selectedCategoriaId" 
                        (ngModelChange)="onCategoriaChange($event)"
                        class="categoria-select">
                        <option *ngFor="let categoria of categorias" [value]="categoria.id">
                          {{ categoria.nome }}
                        </option>
                      </select>
                    </div>
                    
                    <!-- Botão Gerenciar Resultados -->
                    <div class="management-actions" *ngIf="podeGerenciarResultados() && selectedCategoriaId">
                      <button 
                        mat-raised-button 
                        color="accent" 
                        (click)="gerenciarResultados()"
                        class="manage-results-button">
                        <mat-icon>add_task</mat-icon>
                        Adicionar Resultados
                      </button>
                    </div>
                  </div>

                  <!-- Empty State -->
                  <div *ngIf="leaderboardRanking.length === 0" class="empty-state">
                    <mat-icon>leaderboard</mat-icon>
                    <p>Nenhuma classificação disponível ainda</p>
                  </div>

                  <!-- Leaderboard Table - Desktop -->
                  <div *ngIf="leaderboardRanking.length > 0" class="leaderboard-table-container desktop-table">
                    <table class="leaderboard-table">
                      <thead>
                        <tr>
                          <th class="rank-col">RANK</th>
                          <th class="name-col">NAME</th>
                          <th class="points-col">POINTS</th>
                          <th *ngFor="let workout of workoutColumns" class="workout-col">{{ workout }}</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr *ngFor="let ranking of leaderboardRanking; let i = index" 
                            [class.podium-row]="ranking.isPodio"
                            [class.gold]="ranking.posicao === 1"
                            [class.silver]="ranking.posicao === 2"
                            [class.bronze]="ranking.posicao === 3">
                          <td class="rank-cell">
                            <span class="rank-number">{{ ranking.posicao }}</span>
                            <mat-icon *ngIf="ranking.isPodio" class="medal-icon">
                              {{ ranking.posicao === 1 ? 'emoji_events' : 'military_tech' }}
                            </mat-icon>
                          </td>
                          <td class="name-cell">
                            <div class="participant-info">
                              <span class="participant-name">{{ ranking.nomeParticipante }}</span>
                              <mat-icon *ngIf="ranking.isEquipe" class="team-icon">groups</mat-icon>
                            </div>
                          </td>
                          <td class="points-cell">
                            <span class="points-value">{{ ranking.pontuacaoTotal }}</span>
                          </td>
                          <td *ngFor="let workout of workoutColumns" class="workout-cell">
                            <ng-container *ngIf="getWorkoutPosition(ranking, workout) as pos">
                              <div class="workout-result">
                                <span class="position">{{ pos.posicaoWorkout }}º</span>
                                <span class="result">({{ pos.resultadoFormatado }})</span>
                              </div>
                            </ng-container>
                            <ng-container *ngIf="!getWorkoutPosition(ranking, workout)">
                              <span class="no-result">-</span>
                            </ng-container>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- Leaderboard Cards - Mobile -->
                  <div *ngIf="leaderboardRanking.length > 0" class="leaderboard-cards-container mobile-cards">
                    <div *ngFor="let ranking of leaderboardRanking; let i = index" 
                         class="leaderboard-card" 
                         [class.podium-card]="ranking.isPodio"
                         [class.gold-card]="ranking.posicao === 1"
                         [class.silver-card]="ranking.posicao === 2"
                         [class.bronze-card]="ranking.posicao === 3"
                         (click)="toggleAtletaDetails(i)">
                      
                      <div class="card-header">
                        <div class="left-section">
                          <div class="rank-section">
                            <span class="rank-number">{{ ranking.posicao }}</span>
                            <mat-icon *ngIf="ranking.isPodio" class="medal-icon">
                              {{ ranking.posicao === 1 ? 'emoji_events' : 'military_tech' }}
                            </mat-icon>
                          </div>
                          
                          <div class="athlete-info">
                            <div class="athlete-name">
                              <span>{{ ranking.nomeParticipante }}</span>
                              <mat-icon *ngIf="ranking.isEquipe" class="team-icon">groups</mat-icon>
                            </div>
                          </div>
                        </div>

                        <div class="right-section">
                          <div class="athlete-points">{{ ranking.pontuacaoTotal }} pts</div>
                          
                          <div class="expand-icon">
                            <mat-icon [class.expanded]="expandedAtletas[i]">
                              {{ expandedAtletas[i] ? 'keyboard_arrow_up' : 'keyboard_arrow_down' }}
                            </mat-icon>
                          </div>
                        </div>
                      </div>

                      <div class="card-details" [class.expanded]="expandedAtletas[i]" *ngIf="expandedAtletas[i]">
                        <div class="workout-details">
                          <div *ngFor="let workout of workoutColumns" class="workout-item">
                            <ng-container *ngIf="getWorkoutPosition(ranking, workout) as pos">
                              <div class="workout-simple-result">
                                <span class="workout-label">{{ workout }}:</span>
                                <span class="position-result">{{ pos.posicaoWorkout }}º</span>
                                <span class="time-reps">({{ pos.resultadoFormatado }})</span>
                              </div>
                            </ng-container>
                            <ng-container *ngIf="!getWorkoutPosition(ranking, workout)">
                              <span class="no-result-mobile">{{ workout }}: -</span>
                            </ng-container>
                          </div>
                        </div>
                      </div>
                    </div>
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
                <!-- Loading State -->
                <div *ngIf="workoutLoading" class="loading-state small">
                  <mat-spinner diameter="40"></mat-spinner>
                  <p>Carregando workouts...</p>
                </div>
                
                <!-- Error State -->
                <div *ngIf="workoutError && !workoutLoading" class="error-state small">
                  <mat-icon>error_outline</mat-icon>
                  <h3>Erro ao carregar workouts</h3>
                  <p>Não foi possível carregar os exercícios do evento.</p>
                </div>
                
                <!-- Empty State -->
                <div *ngIf="!workoutLoading && !workoutError && (!workoutsByCategory || workoutsByCategory.length === 0)" class="empty-state small">
                  <mat-icon>fitness_center</mat-icon>
                  <h3>Workouts ainda não divulgados</h3>
                  <p>Os exercícios para este evento ainda não foram publicados.</p>
                </div>

                <!-- Content State -->
                <div *ngIf="!workoutLoading && !workoutError && workoutsByCategory && workoutsByCategory.length > 0" class="workouts-content">
                  <div *ngFor="let group of workoutsByCategory" class="category-group">
                    <h3 class="category-title">{{ group.categoria.nome }}</h3>
                    <div class="workouts-grid">
                      <mat-card *ngFor="let workout of group.workouts" class="workout-card">
                        <mat-card-header>
                          <mat-card-title>{{ workout.nome }}</mat-card-title>
                        </mat-card-header>
                        <mat-card-content>
                          <div class="workout-details">
                            <div class="detail-item">
                              <span>{{ workout.descricao }}</span>
                            </div>
                          </div>
                        </mat-card-content>
                      </mat-card>
                    </div>
                  </div>
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
                  <div *ngIf="anexoData && anexoData.length === 0" class="empty-state">
                    <mat-icon>folder</mat-icon>
                    <p>Nenhum documento disponível</p>
                  </div>
                  <div *ngFor="let anexo of anexoData" class="anexo-item">
                    <mat-icon>{{ getAnexoIcon(anexo.tipoMime) }}</mat-icon>
                    <div class="anexo-info">
                      <h4>{{ anexo.nomeArquivo }}</h4>
                      <p *ngIf="anexo.descricao">{{ anexo.descricao }}</p>
                      <small>{{ anexo.tamanhoFormatado }} • {{ anexo.extensao.toUpperCase() }}</small>
                    </div>
                    <button mat-icon-button (click)="downloadAnexo(anexo)">
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
  timelineData: Timeline | null = null;
  timelineLoading = false;
  timelineError = false;

  // Leaderboard
  leaderboardData: LeaderboardResponse | null = null;
  leaderboardLoading = false;
  leaderboardError = false;
  categorias: Categoria[] = [];

  // Novo leaderboard ranking
  leaderboardRanking: LeaderboardRanking[] = [];
  selectedCategoriaId: number | null = null;
  expandedAtletas: boolean[] = [];
  workoutColumns: string[] = [];

  // Workouts
  workoutData: Workout[] = [];
  workoutsByCategory: WorkoutsByCategory[] = [];
  workoutLoading = false;
  workoutError = false;

  // Anexos
  anexoData: AnexoResponse | null = null;
  anexoLoading = false;
  anexoError = false;

  // Inscrições (categorias disponíveis)
  categoriasInscricao: CategoriaInscricao[] = [];
  categoriasCarregando = false;
  categoriasError = false;
  carrinhoCategorias: Map<number, number> = new Map(); // categoriaId -> quantidade

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventoService: EventoService,
    private timelineService: TimelineService,
    private leaderboardService: LeaderboardService,
    private workoutService: WorkoutService,
    private anexoService: AnexoService,
    private categoriaService: CategoriaService,
    private authService: AuthService,
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
        // Carregar categorias após carregar o evento
        this.carregarCategorias();
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
    if (this.categorias.length > 0 && this.selectedCategoriaId) return; // Cache

    // Primeiro carrega categorias
    this.leaderboardLoading = true;
    this.leaderboardError = false;

    this.leaderboardService.buscarCategoriasPorEvento(this.eventoId).subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        if (categorias.length > 0) {
          this.selectedCategoriaId = categorias[0].id;
          this.carregarRankingCategoria(this.selectedCategoriaId);
        } else {
          this.leaderboardLoading = false;
          this.leaderboardRanking = [];
        }
      },
      error: (error) => {
        this.leaderboardLoading = false;
        this.leaderboardError = true;
        console.error('Erro ao carregar categorias:', error);
      }
    });
  }

  carregarRankingCategoria(categoriaId: number): void {
    this.leaderboardLoading = true;
    this.leaderboardError = false;

    this.leaderboardService.getRankingCategoria(this.eventoId, categoriaId).subscribe({
      next: (rankings) => {
        this.leaderboardRanking = rankings;
        this.extractWorkoutColumns();
        this.leaderboardLoading = false;
      },
      error: (error) => {
        this.leaderboardLoading = false;
        this.leaderboardError = true;
        console.error('Erro ao carregar ranking da categoria:', error);
      }
    });
  }

  onCategoriaChange(categoriaId: number): void {

    this.selectedCategoriaId = categoriaId;


    this.carregarRankingCategoria(categoriaId);
  }

  private extractWorkoutColumns(): void {
    if (this.leaderboardRanking.length > 0) {
      const workouts = this.leaderboardRanking[0].posicoesWorkouts || [];
      this.workoutColumns = workouts.map(w => w.nomeWorkout);
    }
  }

  getWorkoutPosition(ranking: LeaderboardRanking, workoutName: string): WorkoutPosicao | null {
    if (!ranking.posicoesWorkouts) return null;
    return ranking.posicoesWorkouts.find(p => p.nomeWorkout === workoutName) || null;
  }

  toggleAtletaDetails(index: number): void {
    // Inicializar array se necessário
    if (this.expandedAtletas.length !== this.leaderboardRanking.length) {
      this.expandedAtletas = new Array(this.leaderboardRanking.length).fill(false);
    }
    // Toggle do estado de expansão
    this.expandedAtletas[index] = !this.expandedAtletas[index];
  }

  carregarWorkouts(): void {
    if (this.workoutData && this.workoutData.length > 0) return; // Cache

    this.workoutLoading = true;
    this.workoutError = false;

    // Garantir que os arrays estão inicializados
    this.workoutData = [];
    this.workoutsByCategory = [];

    this.workoutService.buscarWorkoutsPorEvento(this.eventoId).subscribe({
      next: (data) => {

        // Garantir que data é um array válido
        if (data && Array.isArray(data)) {
          this.workoutData = data;
          this.workoutsByCategory = this.agruparWorkoutsPorCategoria(data);
        } else {
          this.workoutData = [];
          this.workoutsByCategory = [];
        }

        this.workoutLoading = false;
      },
      error: (error) => {
        console.error('🏋️ Erro ao carregar workouts:', error);
        this.workoutLoading = false;
        this.workoutError = true;
        // Garantir que arrays estão inicializados mesmo em erro
        this.workoutData = [];
        this.workoutsByCategory = [];
      }
    });
  }

  private agruparWorkoutsPorCategoria(workouts: Workout[]): WorkoutsByCategory[] {

    if (!workouts || !Array.isArray(workouts) || workouts.length === 0) {
      return [];
    }

    const categoriasMap = new Map<string, WorkoutsByCategory>();

    workouts.forEach(workout => {
      if (!workout) return; // Pula workouts inválidos

      // Se o workout não tem categorias, criar uma categoria "Sem categoria"
      if (!workout.nomesCategorias || workout.quantidadeCategorias === 0) {
        const semCategoriaKey = 'sem-categoria';
        if (!categoriasMap.has(semCategoriaKey)) {
          categoriasMap.set(semCategoriaKey, {
            categoria: { id: 0, nome: 'Sem categoria específica', ativa: true },
            workouts: []
          });
        }
        const categoria = categoriasMap.get(semCategoriaKey);
        if (categoria) {
          categoria.workouts.push(workout);
        }
      } else {
        // Separar por vírgula se há múltiplas categorias
        const nomesCategorias = workout.nomesCategorias.split(', ');

        nomesCategorias.forEach((nomeCategoria, index) => {
          const categoriaKey = nomeCategoria.trim();
          if (!categoriasMap.has(categoriaKey)) {
            categoriasMap.set(categoriaKey, {
              categoria: {
                id: index + 1, // ID temporário para categorias múltiplas
                nome: nomeCategoria.trim(),
                ativa: true
              },
              workouts: []
            });
          }
          const categoria = categoriasMap.get(categoriaKey);
          if (categoria) {
            categoria.workouts.push(workout);
          }
        });
      }
    });

    // Converter Map para Array e ordenar por nome da categoria
    const resultado = Array.from(categoriasMap.values())
      .sort((a, b) => a.categoria.nome.localeCompare(b.categoria.nome));

    return resultado;
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

  downloadAnexo(anexo: Anexo): void {
    this.anexoService.downloadAnexo(anexo.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = anexo.nomeArquivo;
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
    // Se o evento tem uma imagem própria, usar ela
    if (evento.imagemUrl && evento.imagemUrl.trim() !== '') {
      // Se for uma URL relativa, adicionar o host da API
      if (evento.imagemUrl.startsWith('/api/')) {
        return `http://localhost:8080${evento.imagemUrl}`;
      }
      return evento.imagemUrl;
    }

    // Fallback: usar imagens placeholder baseadas no ID do evento
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

  getAnexoIcon(tipoMime: string): string {
    if (tipoMime.startsWith('image/')) {
      return 'photo';
    } else if (tipoMime.startsWith('video/')) {
      return 'videocam';
    } else if (tipoMime === 'application/pdf') {
      return 'picture_as_pdf';
    } else if (tipoMime.includes('word') || tipoMime.includes('document')) {
      return 'description';
    } else if (tipoMime.includes('sheet') || tipoMime.includes('excel')) {
      return 'table_chart';
    } else if (tipoMime.includes('presentation') || tipoMime.includes('powerpoint')) {
      return 'slideshow';
    } else if (tipoMime.startsWith('text/')) {
      return 'article';
    } else {
      return 'insert_drive_file';
    }
  }

  getWorkoutTypeLabel(tipo: string): string {
    switch (tipo) {
      case 'REPS':
        return 'Repetições';
      case 'TEMPO':
        return 'Tempo';
      case 'PESO':
        return 'Peso';
      default:
        return tipo;
    }
  }

  // ===============================================
  // MÉTODOS PARA GERENCIAMENTO DE RESULTADOS
  // ===============================================

  /**
   * Verifica se o usuário pode gerenciar resultados do evento
   * (apenas criador do evento ou admin)
   */
  podeGerenciarResultados(): boolean {
    if (!this.authService.isAuthenticated()) {
      return false;
    }

    // Admin sempre pode gerenciar
    if (AuthHelpers.isAdmin(this.authService)) {
      return true;
    }

    // Verificar se é o criador do evento
    const userEmail = AuthHelpers.getCurrentUserEmail(this.authService);
    return this.evento?.organizadorEmail === userEmail;
  }

  /**
   * Navega para a página de gerenciamento de resultados da categoria selecionada
   */
  gerenciarResultados(): void {
    if (!this.podeGerenciarResultados() || !this.selectedCategoriaId) {
      this.snackBar.open('Você não tem permissão para gerenciar resultados', 'Fechar', {
        duration: 3000
      });
      return;
    }

    // Navegar para a página de gerenciamento
    this.router.navigate(['/eventos', this.eventoId, 'categoria', this.selectedCategoriaId, 'resultados']);
  }

  // ===============================================
  // MÉTODOS PARA GERENCIAMENTO DE INSCRIÇÕES
  // ===============================================

  /**
   * Carrega as categorias disponíveis para inscrição
   */
  carregarCategorias(): void {
    if (!this.evento || !this.evento.podeReceberInscricoes) {
      return;
    }

    this.categoriasCarregando = true;
    this.categoriasError = false;

    this.categoriaService.buscarCategoriasPorEvento(this.eventoId).subscribe({
      next: (categorias) => {
        // Filtrar apenas categorias ativas
        this.categoriasInscricao = this.categoriaService.filtrarCategoriasAtivas(categorias);
        this.categoriasCarregando = false;
      },
      error: (error) => {
        this.categoriasCarregando = false;
        this.categoriasError = true;
        console.error('Erro ao carregar categorias:', error);
      }
    });
  }

  /**
   * Adiciona uma unidade de uma categoria ao carrinho
   */
  adicionarCategoria(categoriaId: number): void {
    const quantidadeAtual = this.carrinhoCategorias.get(categoriaId) || 0;
    this.carrinhoCategorias.set(categoriaId, quantidadeAtual + 1);
  }

  /**
   * Remove uma unidade de uma categoria do carrinho
   */
  removerCategoria(categoriaId: number): void {
    const quantidadeAtual = this.carrinhoCategorias.get(categoriaId) || 0;
    if (quantidadeAtual > 0) {
      const novaQuantidade = quantidadeAtual - 1;
      if (novaQuantidade === 0) {
        this.carrinhoCategorias.delete(categoriaId);
      } else {
        this.carrinhoCategorias.set(categoriaId, novaQuantidade);
      }
    }
  }

  /**
   * Retorna a quantidade de uma categoria no carrinho
   */
  getQuantidadeCategoria(categoriaId: number): number {
    return this.carrinhoCategorias.get(categoriaId) || 0;
  }

  /**
   * Calcula o valor total do carrinho
   */
  calcularValorTotal(): number {
    return this.categoriaService.calcularValorTotal(this.categoriasInscricao, this.carrinhoCategorias);
  }

  /**
   * Procede com o processo de inscrição
   */
  prosseguirInscricao(): void {
    if (this.carrinhoCategorias.size === 0) {
      this.snackBar.open('Selecione pelo menos uma categoria para inscrição', 'Fechar', {
        duration: 3000
      });
      return;
    }

    // Preparar dados para o processo de inscrição
    const categoriasEscolhidas: Array<{ categoriaId: number, quantidade: number }> = [];
    this.carrinhoCategorias.forEach((quantidade, categoriaId) => {
      categoriasEscolhidas.push({ categoriaId, quantidade });
    });

    // Navegar para o formulário de inscrição
    this.router.navigate(['/inscricoes', 'nova', this.eventoId], {
      queryParams: {
        categorias: JSON.stringify(categoriasEscolhidas)
      }
    });
  }

  /**
   * Verifica se há itens no carrinho
   */
  temItensNoCarrinho(): boolean {
    return this.carrinhoCategorias.size > 0 && this.calcularValorTotal() > 0;
  }
}
