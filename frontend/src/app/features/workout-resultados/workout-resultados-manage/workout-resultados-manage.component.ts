import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { EventoService } from '../../../core/services/evento.service';
import { InscricaoService, ParticipanteDTO } from '../../../core/services/inscricao.service';
import { LeaderboardService } from '../../../core/services/leaderboard.service';
import { WorkoutService } from '../../../core/services/workout.service';

import { EventoApiResponse } from '../../../models/evento.model';
import { Categoria } from '../../../models/leaderboard.model';
import {
  LeaderboardSummaryDTO,
  Workout,
  WorkoutResultCreateDTO,
  WorkoutResultStatusDTO,
  WorkoutType
} from '../../../models/workout.model';

@Component({
  selector: 'app-workout-resultados-manage',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTabsModule,
    MatTooltipModule
  ],
  template: `
    <div class="workout-resultados-container">
      <!-- Loading State -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner></mat-spinner>
        <p>Carregando dados...</p>
      </div>

      <!-- Error State -->
      <div class="error-container" *ngIf="hasError && !isLoading">
        <mat-icon>error_outline</mat-icon>
        <h3>Erro ao carregar dados</h3>
        <p>Não foi possível carregar os dados necessários.</p>
        <button mat-raised-button color="primary" (click)="voltar()">
          <mat-icon>arrow_back</mat-icon>
          Voltar
        </button>
      </div>

      <!-- Main Content -->
      <div class="main-content" *ngIf="evento && categoria && !isLoading && !hasError">
        <!-- Header -->
        <div class="header-section">
          <button mat-icon-button (click)="voltar()" class="back-button">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="header-info">
            <h1>Gerenciar Resultados</h1>
            <div class="event-info">
              <span class="event-name">{{ evento.nome }}</span>
              <span class="category-name"> → {{ categoria.nome }}</span>
            </div>
          </div>
        </div>

        <!-- Workout Tabs -->
        <div class="workout-tabs-container" *ngIf="workouts.length > 0">
          <mat-tab-group [(selectedIndex)]="selectedWorkoutIndex" (selectedTabChange)="onWorkoutTabChange($event)">
            <mat-tab *ngFor="let workout of workouts; let i = index" [label]="workout.nome">
              <div class="workout-tab-content">
                
                <!-- Workout Info -->
                <mat-card class="workout-info-card">
                  <mat-card-header>
                    <mat-card-title>
                      <mat-icon>fitness_center</mat-icon>
                      {{ workout.nome }}
                    </mat-card-title>
                    <mat-card-subtitle>{{ workout.descricao }}</mat-card-subtitle>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="workout-details">
                      <div class="detail-item">
                        <strong>Tipo:</strong> {{ getWorkoutTypeLabel(workout.tipo) }}
                      </div>
                      <div class="detail-item">
                        <strong>Unidade:</strong> {{ workout.unidadeMedida }}
                      </div>
                    </div>
                  </mat-card-content>
                </mat-card>

                <!-- Status Card -->
                <mat-card class="status-card" *ngIf="workoutStatus[workout.id]">
                  <mat-card-header>
                    <mat-card-title>
                      <mat-icon>assessment</mat-icon>
                      Status do Workout
                    </mat-card-title>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="status-details">
                      <div class="status-item">
                        <span class="label">Participantes:</span>
                        <span class="value">{{ workoutStatus[workout.id].participantesFinalizados }} / {{ workoutStatus[workout.id].totalParticipantes }}</span>
                      </div>
                      <div class="status-item">
                        <span class="label">Progresso:</span>
                        <span class="value">{{ workoutStatus[workout.id].porcentagemFinalizados.toFixed(1) }}%</span>
                      </div>
                      <div class="status-item">
                        <span class="label">Status:</span>
                        <span class="value" [class.finalizado]="workoutStatus[workout.id].workoutFinalizado">
                          {{ workoutStatus[workout.id].workoutFinalizado ? 'Finalizado' : 'Em andamento' }}
                        </span>
                      </div>
                    </div>
                  </mat-card-content>
                </mat-card>

                <!-- Add Result Form -->
                <mat-card class="add-result-card">
                  <mat-card-header>
                    <mat-card-title>
                      <mat-icon>add</mat-icon>
                      Adicionar Resultado
                    </mat-card-title>
                  </mat-card-header>
                  <mat-card-content>
                    <form [formGroup]="resultForm" (ngSubmit)="adicionarResultado(workout)" class="result-form">
                      <div class="form-row">
                        <mat-form-field appearance="outline" class="participant-field">
                          <mat-label>Participante</mat-label>
                          <mat-select formControlName="participanteId" (selectionChange)="onParticipanteChange($event.value)">
                            <mat-option value="">Selecione um participante...</mat-option>
                            <mat-option *ngFor="let participante of participantes" [value]="participante.id">
                              {{ participante.nome }}
                              <span *ngIf="participante.nomeEquipe" class="team-indicator"> ({{ participante.nomeEquipe }})</span>
                            </mat-option>
                          </mat-select>
                          <mat-error *ngIf="resultForm.get('participanteId')?.hasError('required')">
                            Participante é obrigatório
                          </mat-error>
                        </mat-form-field>

                        <mat-form-field appearance="outline" class="result-field">
                          <mat-label>Resultado ({{ workout.unidadeMedida }})</mat-label>
                          <input matInput formControlName="resultadoValor">
                          <mat-error *ngIf="resultForm.get('resultadoValor')?.hasError('required')">
                            Resultado é obrigatório
                          </mat-error>
                        </mat-form-field>

                        <button 
                          mat-raised-button 
                          color="primary" 
                          type="submit"
                          [disabled]="resultForm.invalid || isSubmitting"
                          class="add-button">
                          <mat-icon>add</mat-icon>
                          {{ isSubmitting ? 'Adicionando...' : 'Adicionar' }}
                        </button>
                      </div>
                    </form>
                  </mat-card-content>
                </mat-card>

                <!-- Results Table -->
                <mat-card class="results-table-card">
                  <mat-card-header>
                    <mat-card-title>
                      <mat-icon>list</mat-icon>
                      Resultados Atuais
                    </mat-card-title>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="table-loading" *ngIf="loadingResults[workout.id]">
                      <mat-spinner diameter="40"></mat-spinner>
                      <p>Carregando resultados...</p>
                    </div>

                    <div class="no-results" *ngIf="!loadingResults[workout.id] && (!workoutResults[workout.id] || workoutResults[workout.id].length === 0)">
                      <mat-icon>info</mat-icon>
                      <p>Nenhum resultado cadastrado ainda</p>
                    </div>

                    <div class="results-table" *ngIf="!loadingResults[workout.id] && workoutResults[workout.id] && workoutResults[workout.id].length > 0">
                      <table mat-table [dataSource]="workoutResults[workout.id]" class="mat-elevation-z2">
                        
                        <ng-container matColumnDef="posicao">
                          <th mat-header-cell *matHeaderCellDef>Posição</th>
                          <td mat-cell *matCellDef="let result">{{ result.posicaoWorkout }}º</td>
                        </ng-container>

                        <ng-container matColumnDef="participante">
                          <th mat-header-cell *matHeaderCellDef>Participante</th>
                          <td mat-cell *matCellDef="let result">
                            <div class="participant-cell">
                              {{ result.nomeParticipante }}
                              <mat-icon *ngIf="result.isEquipe" class="team-icon">groups</mat-icon>
                            </div>
                          </td>
                        </ng-container>

                        <ng-container matColumnDef="resultado">
                          <th mat-header-cell *matHeaderCellDef>Resultado</th>
                          <td mat-cell *matCellDef="let result">{{ result.resultadoFormatado }}</td>
                        </ng-container>

                        <ng-container matColumnDef="pontuacao">
                          <th mat-header-cell *matHeaderCellDef>Pontos</th>
                          <td mat-cell *matCellDef="let result">{{ result.pontuacaoWorkout }}</td>
                        </ng-container>

                        <ng-container matColumnDef="status">
                          <th mat-header-cell *matHeaderCellDef>Status</th>
                          <td mat-cell *matCellDef="let result">
                            <span [class.finalizado]="result.finalizado" [class.pendente]="!result.finalizado">
                              {{ result.finalizado ? 'Finalizado' : 'Pendente' }}
                            </span>
                          </td>
                        </ng-container>

                        <ng-container matColumnDef="actions">
                          <th mat-header-cell *matHeaderCellDef>Ações</th>
                          <td mat-cell *matCellDef="let result">
                            <button 
                              mat-icon-button 
                              color="primary"
                              matTooltip="Editar resultado"
                              (click)="editarResultado(result)">
                              <mat-icon>edit</mat-icon>
                            </button>
                            <button 
                              mat-icon-button 
                              color="warn"
                              matTooltip="Remover resultado"
                              (click)="removerResultado(workout, result)">
                              <mat-icon>delete</mat-icon>
                            </button>
                          </td>
                        </ng-container>

                        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
                      </table>
                    </div>
                  </mat-card-content>
                </mat-card>

              </div>
            </mat-tab>
          </mat-tab-group>
        </div>

        <!-- No Workouts State -->
        <div class="no-workouts" *ngIf="workouts.length === 0 && !isLoading">
          <mat-icon>fitness_center</mat-icon>
          <h3>Nenhum workout encontrado</h3>
          <p>Não há workouts disponíveis para esta categoria.</p>
        </div>
      </div>
    </div>
  `,
  styleUrl: './workout-resultados-manage.component.scss'
})
export class WorkoutResultadosManageComponent implements OnInit {
  // Route params
  eventoId: number = 0;
  categoriaId: number = 0;

  // Data
  evento: EventoApiResponse | null = null;
  categoria: Categoria | null = null;
  workouts: Workout[] = [];
  participantes: ParticipanteDTO[] = [];

  // States
  isLoading = false;
  hasError = false;
  isSubmitting = false;
  selectedWorkoutIndex = 0;

  // Results data
  workoutResults: { [workoutId: number]: LeaderboardSummaryDTO[] } = {};
  workoutStatus: { [workoutId: number]: WorkoutResultStatusDTO } = {};
  loadingResults: { [workoutId: number]: boolean } = {};

  // Form
  resultForm: FormGroup;

  // Table
  displayedColumns: string[] = ['posicao', 'participante', 'resultado', 'pontuacao', 'status', 'actions'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private eventoService: EventoService,
    private leaderboardService: LeaderboardService,
    private workoutService: WorkoutService,
    private inscricaoService: InscricaoService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.resultForm = this.formBuilder.group({
      participanteId: ['', Validators.required],
      resultadoValor: ['', Validators.required],
      isEquipe: [false],
      finalizado: [true]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.eventoId = +params['eventoId'];
      this.categoriaId = +params['categoriaId'];

      if (this.eventoId && this.categoriaId) {
        this.carregarDados();
      } else {
        this.hasError = true;
      }
    });
  }

  carregarDados(): void {
    console.log('🔄 [DEBUG] Iniciando carregarDados()');
    console.log('🔄 [DEBUG] eventoId:', this.eventoId, 'categoriaId:', this.categoriaId);

    this.isLoading = true;
    this.hasError = false;

    console.log('🔄 [DEBUG] Fazendo requisições HTTP...');

    forkJoin({
      evento: this.eventoService.buscarEventoPorId(this.eventoId),
      categorias: this.leaderboardService.buscarCategoriasPorEvento(this.eventoId),
      workouts: this.workoutService.buscarWorkoutsPorEvento(this.eventoId),
      participantes: this.inscricaoService.getParticipantesByCategoria(this.eventoId, this.categoriaId)
    }).subscribe({
      next: (data) => {
        console.log('✅ [DEBUG] ForkJoin completado com sucesso');
        console.log('✅ [DEBUG] Dados recebidos:', {
          evento: !!data.evento,
          categorias: data.categorias?.length || 0,
          workouts: data.workouts?.length || 0,
          participantes: data.participantes?.length || 0
        });

        this.evento = data.evento;
        this.categoria = data.categorias.find(c => c.id === this.categoriaId) || null;
        this.workouts = data.workouts.filter(w => w.nomesCategorias.includes(this.categoria?.nome || ''));
        this.participantes = data.participantes;

        console.log('👥 [DEBUG] Participantes carregados:', this.participantes);
        console.log('🏃 [DEBUG] Categoria encontrada:', this.categoria?.nome);
        console.log('🏋️ [DEBUG] Workouts filtrados:', this.workouts.length);

        if (this.workouts.length > 0) {
          this.carregarResultadosWorkout(this.workouts[0]);
        }

        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ [ERROR] Falha no forkJoin:', error);
        console.error('❌ [ERROR] Detalhes do erro:', JSON.stringify(error, null, 2));

        this.isLoading = false;
        this.hasError = true;
      }
    });
  }

  onWorkoutTabChange(event: any): void {
    const workout = this.workouts[event.index];
    if (workout) {
      this.carregarResultadosWorkout(workout);
    }
  }

  carregarResultadosWorkout(workout: Workout): void {
    if (this.workoutResults[workout.id] && this.workoutStatus[workout.id]) {
      return; // Cache
    }

    this.loadingResults[workout.id] = true;

    forkJoin({
      resultados: this.workoutService.getResultadosWorkout(workout.id, this.categoriaId),
      status: this.workoutService.getStatusWorkout(workout.id, this.categoriaId)
    }).subscribe({
      next: (data) => {
        this.workoutResults[workout.id] = data.resultados;
        this.workoutStatus[workout.id] = data.status;
        this.loadingResults[workout.id] = false;
      },
      error: (error) => {
        this.loadingResults[workout.id] = false;
        console.error(`Erro ao carregar resultados do workout ${workout.id}:`, error);
      }
    });
  }

  onParticipanteChange(participanteId: number): void {
    const participante = this.participantes.find(p => p.id === participanteId);
    if (participante) {
      // Se o participante tem nomeEquipe preenchido, é uma equipe
      // Se não tem nomeEquipe, é um atleta individual
      const isEquipe = !!(participante.nomeEquipe && participante.nomeEquipe.trim());
      this.resultForm.patchValue({
        participanteId: participanteId,
        isEquipe: isEquipe
      });
    }
  }

  adicionarResultado(workout: Workout): void {
    if (this.resultForm.invalid) return;

    this.isSubmitting = true;
    const formValues = this.resultForm.value;

    const novoResultado: WorkoutResultCreateDTO = {
      eventoId: this.eventoId,
      categoriaId: this.categoriaId,
      participanteId: formValues.participanteId,
      isEquipe: formValues.isEquipe,
      resultadoValor: formValues.resultadoValor,
      finalizado: formValues.finalizado
    };

    this.workoutService.adicionarResultado(workout.id, novoResultado).subscribe({
      next: () => {
        this.snackBar.open('Resultado adicionado com sucesso!', 'Fechar', { duration: 3000 });
        this.resultForm.reset({ isEquipe: false, finalizado: true });
        this.recarregarResultadosWorkout(workout);
        this.isSubmitting = false;
      },
      error: (error) => {
        this.snackBar.open('Erro ao adicionar resultado', 'Fechar', { duration: 5000 });
        console.error('Erro ao adicionar resultado:', error);
        this.isSubmitting = false;
      }
    });
  }

  editarResultado(result: LeaderboardSummaryDTO): void {
    // TODO: Implementar dialog de edição
    console.log('Editar resultado:', result);
  }

  removerResultado(workout: Workout, result: LeaderboardSummaryDTO): void {
    if (!confirm(`Tem certeza que deseja remover o resultado de ${result.nomeParticipante}?`)) {
      return;
    }

    const removerMethod = result.isEquipe
      ? this.workoutService.removerResultadoEquipe(workout.id, result.equipeId!)
      : this.workoutService.removerResultadoAtleta(workout.id, result.atletaId!);

    removerMethod.subscribe({
      next: () => {
        this.snackBar.open('Resultado removido com sucesso!', 'Fechar', { duration: 3000 });
        this.recarregarResultadosWorkout(workout);
      },
      error: (error) => {
        this.snackBar.open('Erro ao remover resultado', 'Fechar', { duration: 5000 });
        console.error('Erro ao remover resultado:', error);
      }
    });
  }

  private recarregarResultadosWorkout(workout: Workout): void {
    delete this.workoutResults[workout.id];
    delete this.workoutStatus[workout.id];
    this.carregarResultadosWorkout(workout);
  }

  getWorkoutTypeLabel(tipo: WorkoutType): string {
    switch (tipo) {
      case WorkoutType.REPS:
        return 'Repetições';
      case WorkoutType.TEMPO:
        return 'Tempo';
      case WorkoutType.PESO:
        return 'Peso';
      default:
        return tipo;
    }
  }

  voltar(): void {
    this.router.navigate(['/eventos', this.eventoId]);
  }
}