import { CommonModule } from '@angular/common';
import { Component, OnInit, OnChanges, SimpleChanges, Input } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
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
  WorkoutResultUpdateDTO,
  WorkoutResultStatusDTO,
  WorkoutType
} from '../../../models/workout.model';

import { EditResultadoDialogComponent } from '../edit-resultado-dialog/edit-resultado-dialog.component';

@Component({
  selector: 'app-workout-resultados-manage',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
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
        <!-- Header (only for standalone mode) -->
        <div class="header-section" *ngIf="!isEmbedded">
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

        <!-- Simple Header (for embedded mode) -->
        <div class="embedded-header" *ngIf="isEmbedded">
          <h3>{{ categoria?.nome }}</h3>
          <p class="embedded-description">Gerencie os resultados dos participantes desta categoria</p>
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

                <!-- Bulk Result Entry -->
                <mat-card class="bulk-result-card">
                  <mat-card-header>
                    <mat-card-title>
                      <mat-icon>format_list_bulleted</mat-icon>
                      Gerenciar Resultados ({{ getPreenchidosCount(workout.id) }}/{{ participantes.length }})
                    </mat-card-title>
                    <div class="header-actions">
                      <button
                        mat-stroked-button
                        (click)="limparTodos(workout.id)"
                        [disabled]="isSaving">
                        <mat-icon>clear_all</mat-icon>
                        Limpar Tudo
                      </button>
                      <button
                        mat-raised-button
                        color="primary"
                        (click)="salvarTodos(workout)"
                        [disabled]="!temAlteracoes(workout.id) || isSaving">
                        <mat-icon>save</mat-icon>
                        {{ isSaving ? 'Salvando...' : 'Salvar Todos' }}
                      </button>
                    </div>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="bulk-result-table">
                      <table>
                        <thead>
                          <tr>
                            <th class="col-participante">Participante</th>
                            <th class="col-resultado">Resultado ({{ workout.unidadeMedida }})</th>
                            <th class="col-status">Status</th>
                            <th class="col-actions">Ações</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr *ngFor="let participante of participantes; let i = index"
                              [class.has-result]="getResultadoExistente(workout.id, participante)"
                              [class.has-changes]="resultadosTemp[getParticipanteKey(workout.id, participante)]">
                            <td class="col-participante">
                              <div class="participant-info">
                                <span class="participant-name">{{ participante.nome }}</span>
                                <mat-icon *ngIf="participante.nomeEquipe" class="team-icon" matTooltip="Equipe">groups</mat-icon>
                              </div>
                            </td>
                            <td class="col-resultado">
                              <input
                                type="text"
                                class="result-input"
                                [placeholder]="getPlaceholderByType(workout.tipo)"
                                [(ngModel)]="resultadosTemp[getParticipanteKey(workout.id, participante)]"
                                (focus)="onInputFocus(i)"
                                [disabled]="isSaving">
                            </td>
                            <td class="col-status">
                              <span class="status-badge" [class]="getStatusClass(workout.id, participante)">
                                {{ getStatusLabel(workout.id, participante) }}
                              </span>
                            </td>
                            <td class="col-actions">
                              <button
                                mat-icon-button
                                color="warn"
                                *ngIf="getResultadoExistente(workout.id, participante)"
                                (click)="removerResultadoRapido(workout, participante)"
                                matTooltip="Remover resultado"
                                [disabled]="isSaving">
                                <mat-icon>delete</mat-icon>
                              </button>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>

                    <div class="bulk-result-info" *ngIf="participantes.length === 0">
                      <mat-icon>info</mat-icon>
                      <p>Nenhum participante inscrito nesta categoria</p>
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
export class WorkoutResultadosManageComponent implements OnInit, OnChanges {
  // Inputs for embedded mode
  @Input() eventoIdInput?: number;
  @Input() categoriaIdInput?: number;
  @Input() isEmbedded: boolean = false;

  // Route params
  eventoId: number = 0;
  categoriaId: number = 0;

  // Data
  evento: EventoApiResponse | null = null;
  categoria: Categoria | null = null;
  categoriasDisponiveis: Categoria[] = [];
  workouts: Workout[] = [];
  participantes: ParticipanteDTO[] = [];

  // States
  isLoading = false;
  hasError = false;
  isSubmitting = false;
  isSaving = false;
  selectedWorkoutIndex = 0;

  // Results data
  workoutResults: { [workoutId: number]: LeaderboardSummaryDTO[] } = {};
  workoutStatus: { [workoutId: number]: WorkoutResultStatusDTO } = {};
  loadingResults: { [workoutId: number]: boolean } = {};

  // Bulk result management
  resultadosTemp: { [key: string]: string } = {}; // Key format: "workoutId_participanteId_isEquipe"

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
    // Check if component is embedded (inputs provided)
    if (this.isEmbedded && this.eventoIdInput && this.categoriaIdInput) {
      this.eventoId = this.eventoIdInput;
      this.categoriaId = this.categoriaIdInput;
      this.carregarDados();
    } else {
      // Standalone mode: get params from route
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
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Detectar mudança em categoriaIdInput (somente no modo embedded)
    if (this.isEmbedded && changes['categoriaIdInput'] && !changes['categoriaIdInput'].firstChange) {
      const novaCategoriaId = changes['categoriaIdInput'].currentValue;

      if (novaCategoriaId && novaCategoriaId !== this.categoriaId) {
        console.log('🔄 Categoria alterada:', this.categoriaId, '->', novaCategoriaId);
        this.categoriaId = novaCategoriaId;
        this.limparCache();
        this.carregarDados();
      }
    }
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
        this.categoriasDisponiveis = data.categorias;
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
      // Cache já existe, apenas preencher campos se necessário
      this.preencherCamposComResultadosExistentes(workout.id);
      return;
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

        // Pré-preencher campos com resultados existentes
        this.preencherCamposComResultadosExistentes(workout.id);
      },
      error: (error) => {
        this.loadingResults[workout.id] = false;
        console.error(`Erro ao carregar resultados do workout ${workout.id}:`, error);
      }
    });
  }

  /**
   * Preenche os campos temporários com os resultados existentes
   */
  private preencherCamposComResultadosExistentes(workoutId: number): void {
    const resultados = this.workoutResults[workoutId];
    if (!resultados) return;

    resultados.forEach(resultado => {
      // Encontrar participante correspondente
      const participante = this.participantes.find(p => {
        if (resultado.isEquipe) {
          return p.id === resultado.equipeId && p.nomeEquipe;
        } else {
          return p.id === resultado.atletaId && !p.nomeEquipe;
        }
      });

      if (participante) {
        const key = this.getParticipanteKey(workoutId, participante);
        // Preencher apenas se não houver valor digitado pelo usuário
        if (!this.resultadosTemp[key]) {
          this.resultadosTemp[key] = resultado.resultadoValor || '';
        }
      }
    });
  }

  onParticipanteChange(inscricaoId: number): void {
    const participante = this.participantes.find(p => p.inscricaoId === inscricaoId);
    if (participante) {
      // Se o participante tem nomeEquipe preenchido, é uma equipe
      // Se não tem nomeEquipe, é um atleta individual
      const isEquipe = !!(participante.nomeEquipe && participante.nomeEquipe.trim());
      this.resultForm.patchValue({
        participanteId: participante.id,  // Use athlete/team ID for the backend
        isEquipe: isEquipe
      });
    }
  }

  adicionarResultado(workout: Workout): void {
    if (this.resultForm.invalid) return;

    this.isSubmitting = true;
    const formValues = this.resultForm.value;

    // Converter o valor do resultado baseado no tipo do workout
    let resultadoValor: string | number = formValues.resultadoValor;

    if (workout.tipo === 'REPS') {
      // Para repetições, converter para Integer
      resultadoValor = parseInt(formValues.resultadoValor, 10);
    } else if (workout.tipo === 'PESO') {
      // Para peso, converter para Double/Float
      resultadoValor = parseFloat(formValues.resultadoValor);
    }
    // Para TEMPO, manter como string

    const novoResultado: WorkoutResultCreateDTO = {
      eventoId: this.eventoId,
      categoriaId: this.categoriaId,
      participanteId: formValues.participanteId,
      isEquipe: formValues.isEquipe,
      resultadoValor: resultadoValor,
      finalizado: formValues.finalizado,
      observacoes: undefined
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
    // Find the current workout
    const workout = this.workouts[this.selectedWorkoutIndex];
    if (!workout) {
      console.error('❌ Workout não encontrado');
      return;
    }

    console.log('🔍 [EDITAR] Iniciando edição de resultado:', {
      workout: { id: workout.id, nome: workout.nome, tipo: workout.tipo },
      result: {
        id: result.id,
        nomeParticipante: result.nomeParticipante,
        atletaId: result.atletaId,
        equipeId: result.equipeId,
        isEquipe: result.isEquipe,
        resultadoAtual: result.resultadoFormatado
      }
    });

    // Open edit dialog
    const dialogRef = this.dialog.open(EditResultadoDialogComponent, {
      width: '500px',
      data: {
        result: result,
        workout: workout
      }
    });

    dialogRef.afterClosed().subscribe(updatedData => {
      console.log('🔍 [EDITAR] Dialog fechado. Dados retornados:', updatedData);

      if (updatedData) {
        this.atualizarResultado(workout, result, updatedData);
      } else {
        console.log('ℹ️ [EDITAR] Edição cancelada pelo usuário');
      }
    });
  }

  private atualizarResultado(
    workout: Workout,
    result: LeaderboardSummaryDTO,
    updatedData: { resultadoValor: string, finalizado: boolean }
  ): void {
    console.log('🔄 [ATUALIZAR] Preparando atualização:', {
      workoutId: workout.id,
      workoutTipo: workout.tipo,
      isEquipe: result.isEquipe,
      participanteId: result.isEquipe ? result.equipeId : result.atletaId,
      dadosAntigos: result.resultadoFormatado,
      dadosNovos: updatedData
    });

    // Validar IDs antes de enviar
    if (result.isEquipe && !result.equipeId) {
      console.error('❌ [ATUALIZAR] equipeId não encontrado para equipe');
      this.snackBar.open('Erro: ID da equipe não encontrado', 'Fechar', { duration: 5000 });
      return;
    }

    if (!result.isEquipe && !result.atletaId) {
      console.error('❌ [ATUALIZAR] atletaId não encontrado para atleta');
      this.snackBar.open('Erro: ID do atleta não encontrado', 'Fechar', { duration: 5000 });
      return;
    }

    // Converter tipo de dado baseado no tipo do workout
    let resultadoParaEnviar: string | number = updatedData.resultadoValor;

    if (workout.tipo === 'REPS') {
      resultadoParaEnviar = parseInt(updatedData.resultadoValor, 10);
      console.log('🔢 [ATUALIZAR] Convertendo para REPS (Integer):', resultadoParaEnviar);
    } else if (workout.tipo === 'PESO') {
      resultadoParaEnviar = parseFloat(updatedData.resultadoValor);
      console.log('⚖️ [ATUALIZAR] Convertendo para PESO (Float):', resultadoParaEnviar);
    } else {
      console.log('⏱️ [ATUALIZAR] Mantendo como TEMPO (String):', resultadoParaEnviar);
    }

    const updateDTO: WorkoutResultUpdateDTO = {
      resultadoValor: resultadoParaEnviar.toString(),
      finalizado: updatedData.finalizado
    };

    console.log('📤 [ATUALIZAR] Enviando DTO para backend:', updateDTO);

    const updateMethod = result.isEquipe
      ? this.workoutService.atualizarResultadoEquipe(workout.id, result.equipeId!, updateDTO)
      : this.workoutService.atualizarResultadoAtleta(workout.id, result.atletaId!, updateDTO);

    updateMethod.subscribe({
      next: (response) => {
        console.log('✅ [ATUALIZAR] Sucesso! Resposta do backend:', response);
        this.snackBar.open(
          `✅ Resultado de ${result.nomeParticipante} atualizado com sucesso!`,
          'Fechar',
          { duration: 3000 }
        );
        this.recarregarResultadosWorkout(workout);
      },
      error: (error) => {
        console.error('❌ [ATUALIZAR] Erro ao atualizar resultado:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error
        });

        let mensagem = 'Erro ao atualizar resultado';

        if (error.status === 404) {
          mensagem = 'Resultado não encontrado';
        } else if (error.status === 403) {
          mensagem = 'Você não tem permissão para editar este resultado';
        } else if (error.status === 400) {
          mensagem = 'Dados inválidos: ' + (error.error?.message || 'formato incorreto');
        }

        this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
      }
    });
  }

  removerResultado(workout: Workout, result: LeaderboardSummaryDTO): void {
    console.log('🗑️ [REMOVER] Solicitando remoção de resultado:', {
      workout: { id: workout.id, nome: workout.nome },
      result: {
        id: result.id,
        nomeParticipante: result.nomeParticipante,
        atletaId: result.atletaId,
        equipeId: result.equipeId,
        isEquipe: result.isEquipe,
        resultado: result.resultadoFormatado
      }
    });

    if (!confirm(`Tem certeza que deseja remover o resultado de ${result.nomeParticipante}?`)) {
      console.log('ℹ️ [REMOVER] Remoção cancelada pelo usuário');
      return;
    }

    // Validar IDs antes de enviar
    if (result.isEquipe && !result.equipeId) {
      console.error('❌ [REMOVER] equipeId não encontrado para equipe');
      this.snackBar.open('Erro: ID da equipe não encontrado', 'Fechar', { duration: 5000 });
      return;
    }

    if (!result.isEquipe && !result.atletaId) {
      console.error('❌ [REMOVER] atletaId não encontrado para atleta');
      this.snackBar.open('Erro: ID do atleta não encontrado', 'Fechar', { duration: 5000 });
      return;
    }

    console.log('📤 [REMOVER] Chamando service para remover:', {
      workoutId: workout.id,
      participanteId: result.isEquipe ? result.equipeId : result.atletaId,
      tipoParticipante: result.isEquipe ? 'equipe' : 'atleta'
    });

    const removerMethod = result.isEquipe
      ? this.workoutService.removerResultadoEquipe(workout.id, result.equipeId!)
      : this.workoutService.removerResultadoAtleta(workout.id, result.atletaId!);

    removerMethod.subscribe({
      next: () => {
        console.log('✅ [REMOVER] Resultado removido com sucesso!');
        this.snackBar.open(
          `🗑️ Resultado de ${result.nomeParticipante} removido com sucesso!`,
          'Fechar',
          { duration: 3000 }
        );
        this.recarregarResultadosWorkout(workout);
      },
      error: (error) => {
        console.error('❌ [REMOVER] Erro ao remover resultado:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error
        });

        let mensagem = 'Erro ao remover resultado';

        if (error.status === 404) {
          mensagem = 'Resultado não encontrado no banco de dados';
        } else if (error.status === 403) {
          mensagem = 'Você não tem permissão para remover este resultado';
        } else if (error.status === 400) {
          mensagem = 'Erro na requisição: ' + (error.error?.message || 'dados inválidos');
        }

        this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
      }
    });
  }

  private recarregarResultadosWorkout(workout: Workout): void {
    delete this.workoutResults[workout.id];
    delete this.workoutStatus[workout.id];
    this.carregarResultadosWorkout(workout);
  }

  private limparCache(): void {
    console.log('🧹 Limpando cache de resultados e participantes');
    this.workoutResults = {};
    this.workoutStatus = {};
    this.loadingResults = {};
    this.selectedWorkoutIndex = 0;
    this.participantes = [];
    this.workouts = [];
    this.resultForm.reset({ isEquipe: false, finalizado: true });
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

  // ===============================================
  // BULK RESULT MANAGEMENT METHODS
  // ===============================================

  /**
   * Gera chave única para identificar resultado de participante em workout
   */
  getParticipanteKey(workoutId: number, participante: ParticipanteDTO): string {
    const isEquipe = !!(participante.nomeEquipe && participante.nomeEquipe.trim());
    return `${workoutId}_${participante.id}_${isEquipe}`;
  }

  /**
   * Retorna placeholder baseado no tipo de workout
   */
  getPlaceholderByType(tipo: WorkoutType): string {
    switch (tipo) {
      case WorkoutType.REPS:
        return 'Ex: 150';
      case WorkoutType.TEMPO:
        return 'Ex: 10:30 ou 05:45:12';
      case WorkoutType.PESO:
        return 'Ex: 85.5';
      default:
        return 'Digite o resultado';
    }
  }

  /**
   * Busca resultado existente para um participante
   */
  getResultadoExistente(workoutId: number, participante: ParticipanteDTO): LeaderboardSummaryDTO | null {
    const resultados = this.workoutResults[workoutId];
    if (!resultados) return null;

    const isEquipe = !!(participante.nomeEquipe && participante.nomeEquipe.trim());

    return resultados.find(r => {
      if (isEquipe) {
        return r.isEquipe && r.equipeId === participante.id;
      } else {
        return !r.isEquipe && r.atletaId === participante.id;
      }
    }) || null;
  }

  /**
   * Retorna classe CSS baseada no status
   */
  getStatusClass(workoutId: number, participante: ParticipanteDTO): string {
    const resultado = this.getResultadoExistente(workoutId, participante);
    const key = this.getParticipanteKey(workoutId, participante);
    const temAlteracao = this.resultadosTemp[key];

    if (resultado && resultado.finalizado) {
      return 'status-finalizado';
    } else if (resultado && !resultado.finalizado) {
      return 'status-pendente';
    } else if (temAlteracao) {
      return 'status-preenchido';
    } else {
      return 'status-vazio';
    }
  }

  /**
   * Retorna label do status
   */
  getStatusLabel(workoutId: number, participante: ParticipanteDTO): string {
    const resultado = this.getResultadoExistente(workoutId, participante);
    const key = this.getParticipanteKey(workoutId, participante);
    const temAlteracao = this.resultadosTemp[key];

    if (resultado && resultado.finalizado) {
      return 'Finalizado';
    } else if (resultado && !resultado.finalizado) {
      return 'Pendente';
    } else if (temAlteracao) {
      return 'Preenchido';
    } else {
      return 'Sem resultado';
    }
  }

  /**
   * Conta quantos resultados foram preenchidos
   */
  getPreenchidosCount(workoutId: number): number {
    const resultados = this.workoutResults[workoutId] || [];
    const resultadosExistentes = resultados.length;

    // Conta campos temporários preenchidos para participantes que ainda não têm resultado
    const camposNovos = this.participantes.filter(p => {
      const key = this.getParticipanteKey(workoutId, p);
      const temResultado = this.getResultadoExistente(workoutId, p);
      return !temResultado && this.resultadosTemp[key];
    }).length;

    return resultadosExistentes + camposNovos;
  }

  /**
   * Verifica se há alterações pendentes
   */
  temAlteracoes(workoutId: number): boolean {
    return this.participantes.some(p => {
      const key = this.getParticipanteKey(workoutId, p);
      const temValor = this.resultadosTemp[key] && this.resultadosTemp[key].trim() !== '';
      const resultado = this.getResultadoExistente(workoutId, p);

      // Tem alteração se:
      // 1. Campo preenchido e não há resultado existente
      // 2. Campo preenchido e valor diferente do resultado existente
      if (temValor) {
        if (!resultado) {
          return true; // Novo resultado
        }
        // Verifica se valor mudou
        return this.resultadosTemp[key] !== resultado.resultadoValor;
      }
      return false;
    });
  }

  /**
   * Limpa todos os campos temporários de um workout
   */
  limparTodos(workoutId: number): void {
    this.participantes.forEach(p => {
      const key = this.getParticipanteKey(workoutId, p);
      delete this.resultadosTemp[key];
    });
  }

  /**
   * Handler para focus em input
   */
  onInputFocus(index: number): void {
    // Pode adicionar lógica adicional aqui se necessário
  }

  /**
   * Salva todos os resultados preenchidos
   */
  salvarTodos(workout: Workout): void {
    if (!this.temAlteracoes(workout.id)) {
      this.snackBar.open('Nenhuma alteração detectada', 'Fechar', { duration: 3000 });
      return;
    }

    this.isSaving = true;
    const promises: Promise<any>[] = [];
    let sucessos = 0;
    let erros = 0;

    this.participantes.forEach(participante => {
      const key = this.getParticipanteKey(workout.id, participante);
      const valorDigitado = this.resultadosTemp[key];

      if (!valorDigitado || valorDigitado.trim() === '') {
        return; // Pula se não tem valor
      }

      const resultadoExistente = this.getResultadoExistente(workout.id, participante);
      const isEquipe = !!(participante.nomeEquipe && participante.nomeEquipe.trim());

      // Converter valor baseado no tipo
      let resultadoValor: string | number = valorDigitado;
      if (workout.tipo === 'REPS') {
        resultadoValor = parseInt(valorDigitado, 10);
      } else if (workout.tipo === 'PESO') {
        resultadoValor = parseFloat(valorDigitado);
      }

      if (resultadoExistente) {
        // Atualizar resultado existente
        const updateDTO: WorkoutResultUpdateDTO = {
          resultadoValor: resultadoValor.toString(),
          finalizado: true
        };

        const updateMethod = isEquipe
          ? this.workoutService.atualizarResultadoEquipe(workout.id, participante.id, updateDTO)
          : this.workoutService.atualizarResultadoAtleta(workout.id, participante.id, updateDTO);

        const promise = updateMethod.toPromise()
          .then(() => {
            sucessos++;
            delete this.resultadosTemp[key];
          })
          .catch(error => {
            erros++;
            console.error(`Erro ao atualizar resultado de ${participante.nome}:`, error);
          });

        promises.push(promise);
      } else {
        // Criar novo resultado
        const createDTO: WorkoutResultCreateDTO = {
          eventoId: this.eventoId,
          categoriaId: this.categoriaId,
          participanteId: participante.id,
          isEquipe: isEquipe,
          resultadoValor: resultadoValor,
          finalizado: true,
          observacoes: undefined
        };

        const promise = this.workoutService.adicionarResultado(workout.id, createDTO).toPromise()
          .then(() => {
            sucessos++;
            delete this.resultadosTemp[key];
          })
          .catch(error => {
            erros++;
            console.error(`Erro ao adicionar resultado de ${participante.nome}:`, error);
          });

        promises.push(promise);
      }
    });

    Promise.all(promises).finally(() => {
      this.isSaving = false;

      if (sucessos > 0) {
        this.snackBar.open(
          `✅ ${sucessos} resultado(s) salvos com sucesso${erros > 0 ? ` (${erros} erro(s))` : ''}`,
          'Fechar',
          { duration: 4000 }
        );
        this.recarregarResultadosWorkout(workout);
      } else if (erros > 0) {
        this.snackBar.open(
          `❌ Erro ao salvar resultados (${erros} erro(s))`,
          'Fechar',
          { duration: 5000 }
        );
      }
    });
  }

  /**
   * Remove resultado rapidamente (sem confirmação para lista)
   */
  removerResultadoRapido(workout: Workout, participante: ParticipanteDTO): void {
    if (!confirm(`Remover resultado de ${participante.nome}?`)) {
      return;
    }

    const resultado = this.getResultadoExistente(workout.id, participante);
    if (!resultado) return;

    const isEquipe = !!(participante.nomeEquipe && participante.nomeEquipe.trim());
    const removerMethod = isEquipe
      ? this.workoutService.removerResultadoEquipe(workout.id, participante.id)
      : this.workoutService.removerResultadoAtleta(workout.id, participante.id);

    removerMethod.subscribe({
      next: () => {
        this.snackBar.open(`Resultado de ${participante.nome} removido`, 'Fechar', { duration: 3000 });

        // Limpar campo temporário se existir
        const key = this.getParticipanteKey(workout.id, participante);
        delete this.resultadosTemp[key];

        this.recarregarResultadosWorkout(workout);
      },
      error: (error) => {
        console.error('Erro ao remover resultado:', error);
        this.snackBar.open('Erro ao remover resultado', 'Fechar', { duration: 5000 });
      }
    });
  }

  voltar(): void {
    if (this.isEmbedded) {
      // Embedded mode: just return (parent component handles navigation)
      return;
    }
    // Standalone mode: navigate back to event details
    this.router.navigate(['/eventos', this.eventoId]);
  }
}