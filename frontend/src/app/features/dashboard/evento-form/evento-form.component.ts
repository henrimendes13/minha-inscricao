import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { EventoService } from '../../../core/services/evento.service';
import { ImagemService } from '../../../core/services/imagem.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { WorkoutService } from '../../../core/services/workout.service';
import { TimelineService } from '../../../core/services/timeline.service';
import { AnexoService } from '../../../core/services/anexo.service';
import { InscricaoService } from '../../../core/services/inscricao.service';
import { InscricaoFormDialogComponent } from '../inscricao-form-dialog/inscricao-form-dialog.component';
import { WorkoutResultadosManageComponent } from '../../workout-resultados/workout-resultados-manage/workout-resultados-manage.component';
import { EventoApiResponse, EventoCreateRequest, EventoUpdateRequest } from '../../../models/evento.model';
import {
  CategoriaCreateRequest,
  CategoriaUpdateRequest,
  CategoriaApiResponse,
  CategoriaSummaryResponse,
  Genero,
  getGeneroLabel,
  getTipoParticipacaoLabel,
  formatarFaixaEtaria
} from '../../../models/categoria.model';
import {
  Workout,
  WorkoutCreateRequest,
  WorkoutUpdateRequest,
  WorkoutApiResponse,
  WorkoutType,
  getTipoWorkoutLabel,
  getUnidadeMedidaLabel
} from '../../../models/workout.model';
import { TipoParticipacao } from '../../../models';
import { Timeline, TimelineCreateRequest, TimelineUpdateRequest } from '../../../models/timeline.model';
import { Anexo } from '../../../models/anexo.model';
import { InscricaoSummaryResponse, StatusInscricao } from '../../../models/inscricao.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-evento-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatExpansionModule,
    MatChipsModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatDialogModule,
    WorkoutResultadosManageComponent
  ],
  templateUrl: './evento-form.component.html',
  styleUrl: './evento-form.component.scss'
})
export class EventoFormComponent implements OnInit {
  modo: 'criar' | 'editar' = 'criar';
  eventoId: number | null = null;
  evento: EventoApiResponse | null = null;

  eventoForm!: FormGroup;
  isLoading = false;
  isSaving = false;
  selectedTabIndex = 0;

  // Propriedades para controle de imagem
  imagemSelecionada: File | null = null;
  imagemPreview: string | null = null;
  imagemAtualUrl: string | null = null;
  isUploadingImagem = false;
  uploadImagemError: string | null = null;

  // Propriedades para controle de categorias
  categorias: CategoriaSummaryResponse[] = [];
  isLoadingCategorias = false;
  categoriaEmEdicao: CategoriaApiResponse | null = null;
  modoCategoria: 'criar' | 'editar' = 'criar';
  formularioCategoria!: FormGroup;
  mostrarFormularioCategoria = false;
  isSavingCategoria = false;

  // Propriedades para controle de workouts
  workouts: Workout[] = [];
  isLoadingWorkouts = false;
  workoutEmEdicao: WorkoutApiResponse | null = null;
  modoWorkout: 'criar' | 'editar' = 'criar';
  formularioWorkout!: FormGroup;
  mostrarFormularioWorkout = false;
  isSavingWorkout = false;

  // Propriedades para controle de timeline
  timeline: Timeline | null = null;
  timelineExists = false;
  formularioTimeline!: FormGroup;
  isLoadingTimeline = false;
  isSavingTimeline = false;

  // Propriedades para controle de anexos
  anexos: Anexo[] = [];
  isLoadingAnexos = false;
  isUploading = false;
  isDragging = false;

  // Propriedades para controle de inscrições
  inscricoes: InscricaoSummaryResponse[] = [];
  inscricoesFiltradas: InscricaoSummaryResponse[] = [];
  isLoadingInscricoes = false;
  filtrosInscricoes = {
    status: null as StatusInscricao | null,
    categoriaId: null as number | null,
    nomeParticipante: ''
  };
  estatisticasInscricoes = {
    total: 0,
    confirmadas: 0,
    pendentes: 0,
    canceladas: 0
  };

  // Propriedades para controle de resultados
  categoriaSelecionadaResultados: number | null = null;

  estadosBrasileiros = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  constructor(
    private fb: FormBuilder,
    private eventoService: EventoService,
    private imagemService: ImagemService,
    private categoriaService: CategoriaService,
    private workoutService: WorkoutService,
    private timelineService: TimelineService,
    private anexoService: AnexoService,
    private inscricaoService: InscricaoService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.detectarModo();
    this.inicializarFormulario();
    this.inicializarFormularioCategoria();
    this.inicializarFormularioWorkout();
    this.inicializarFormularioTimeline();

    if (this.modo === 'editar' && this.eventoId) {
      this.carregarEvento();
      this.carregarCategorias();
      this.carregarWorkouts();
      this.carregarTimeline();
      this.carregarAnexos();
      this.carregarInscricoes();
    }
  }

  detectarModo(): void {
    // Verifica se existe :id ou :eventoId na rota para determinar modo
    this.eventoId = this.route.snapshot.params['id'] || this.route.snapshot.params['eventoId'];

    if (this.eventoId) {
      this.modo = 'editar';
    } else {
      this.modo = 'criar';
    }
  }

  inicializarFormulario(): void {
    this.eventoForm = this.fb.group({
      // Aba 1 - Informações Básicas
      nome: ['', [Validators.required, Validators.maxLength(200)]],
      descricao: ['', [Validators.maxLength(5000)]],
      dataInicioDoEvento: ['', [Validators.required]],
      dataFimDoEvento: ['', [Validators.required]],
      cidade: ['', [Validators.maxLength(100)]],
      estado: ['', [Validators.maxLength(50)]],
      endereco: ['', [Validators.maxLength(300)]],

      // Aba 2 - Imagem (futuro)
      imagemUrl: [''],

      // Outras abas serão adicionadas futuramente
    });
  }

  carregarEvento(): void {
    if (!this.eventoId) return;

    this.isLoading = true;
    this.eventoService.buscarEventoPorId(this.eventoId).subscribe({
      next: (evento) => {
        this.evento = evento;
        this.preencherFormulario(evento);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar evento:', error);
        this.showSnackBar('Erro ao carregar evento', 'error');
        this.isLoading = false;
        this.voltarParaLista();
      }
    });
  }

  preencherFormulario(evento: EventoApiResponse): void {
    // Converter datas do backend (string) para Date
    const dataInicio = this.parseDataBackend(evento.dataInicioDoEvento);
    const dataFim = this.parseDataBackend(evento.dataFimDoEvento);

    this.eventoForm.patchValue({
      nome: evento.nome,
      descricao: evento.descricao || '',
      dataInicioDoEvento: dataInicio,
      dataFimDoEvento: dataFim,
      cidade: evento.cidade || '',
      estado: evento.estado || '',
      endereco: evento.endereco || '',
      imagemUrl: evento.imagemUrl || ''
    });

    // Carregar imagem atual se existir
    if (evento.imagemUrl) {
      this.imagemAtualUrl = evento.imagemUrl;
    }
  }

  parseDataBackend(dataStr: string): Date {
    // Formato esperado do backend: yyyy-MM-dd'T'HH:mm:ss ou similar
    return new Date(dataStr);
  }

  onSubmit(): void {
    if (this.eventoForm.invalid) {
      this.eventoForm.markAllAsTouched();
      this.showSnackBar('Por favor, preencha todos os campos obrigatórios', 'error');
      return;
    }

    this.isSaving = true;
    const formValue = this.eventoForm.value;

    // Converter datas para formato do backend (dd-MM-yyyy)
    const dataInicio = this.eventoService.converterDataParaBackend(formValue.dataInicioDoEvento);
    const dataFim = this.eventoService.converterDataParaBackend(formValue.dataFimDoEvento);

    if (this.modo === 'criar') {
      this.criarEvento(dataInicio, dataFim, formValue);
    } else {
      this.atualizarEvento(dataInicio, dataFim, formValue);
    }
  }

  criarEvento(dataInicio: string, dataFim: string, formValue: any): void {
    const novoEvento: EventoCreateRequest = {
      nome: formValue.nome,
      dataInicioDoEvento: dataInicio,
      dataFimDoEvento: dataFim,
      descricao: formValue.descricao || undefined,
      cidade: formValue.cidade || undefined,
      estado: formValue.estado || undefined,
      endereco: formValue.endereco || undefined
    };

    this.eventoService.criarEvento(novoEvento).subscribe({
      next: (evento) => {
        this.showSnackBar('Evento criado com sucesso!', 'success');
        // Redirecionar para modo de edição do evento recém-criado
        this.router.navigate(['/dashboard/eventos', evento.id, 'editar']);
      },
      error: (error) => {
        console.error('Erro ao criar evento:', error);
        this.isSaving = false;
        this.showSnackBar('Erro ao criar evento. Verifique os dados e tente novamente.', 'error');
      }
    });
  }

  atualizarEvento(dataInicio: string, dataFim: string, formValue: any): void {
    if (!this.eventoId) return;

    const eventoAtualizado: EventoUpdateRequest = {
      nome: formValue.nome,
      dataInicioDoEvento: dataInicio,
      dataFimDoEvento: dataFim,
      descricao: formValue.descricao || undefined,
      cidade: formValue.cidade || undefined,
      estado: formValue.estado || undefined,
      endereco: formValue.endereco || undefined
    };

    this.eventoService.atualizarEvento(this.eventoId, eventoAtualizado).subscribe({
      next: (evento) => {
        this.evento = evento;
        this.showSnackBar('Evento atualizado com sucesso!', 'success');
        this.isSaving = false;
      },
      error: (error) => {
        console.error('Erro ao atualizar evento:', error);
        this.isSaving = false;
        this.showSnackBar('Erro ao atualizar evento. Verifique os dados e tente novamente.', 'error');
      }
    });
  }

  voltarParaLista(): void {
    this.router.navigate(['/dashboard/eventos']);
  }

  getTitulo(): string {
    if (this.modo === 'criar') {
      return 'Criar Novo Evento';
    }
    return this.evento ? `Editar Evento: ${this.evento.nome}` : 'Editar Evento';
  }

  onCategoriaResultadosChange(categoriaId: number): void {
    this.categoriaSelecionadaResultados = categoriaId;
  }

  getErrorMessage(fieldName: string): string {
    const field = this.eventoForm.get(fieldName);

    if (field?.hasError('required')) {
      return 'Este campo é obrigatório';
    }

    if (field?.hasError('maxlength')) {
      const maxLength = field.errors?.['maxlength'].requiredLength;
      return `Máximo de ${maxLength} caracteres`;
    }

    return '';
  }

  private showSnackBar(message: string, type: 'success' | 'error'): void {
    this.snackBar.open(message, 'Fechar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: type === 'success' ? ['success-snackbar'] : ['error-snackbar']
    });
  }

  // ========== MÉTODOS DE GERENCIAMENTO DE IMAGEM ==========

  /**
   * Captura arquivo selecionado e gera preview
   */
  onImagemSelecionada(event: any): void {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    // Validar imagem
    const validacao = this.imagemService.validarImagem(file);
    if (!validacao.isValid) {
      this.uploadImagemError = validacao.errorMessage || 'Erro ao validar imagem';
      this.showSnackBar(this.uploadImagemError || 'Erro ao validar imagem', 'error');
      this.limparSelecaoImagem();
      return;
    }

    // Armazenar arquivo e limpar erro
    this.imagemSelecionada = file;
    this.uploadImagemError = null;

    // Gerar preview usando FileReader
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.imagemPreview = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  /**
   * Faz upload da imagem selecionada
   */
  uploadImagem(): void {
    if (!this.imagemSelecionada || !this.eventoId) {
      this.showSnackBar('Nenhuma imagem selecionada ou evento inválido', 'error');
      return;
    }

    this.isUploadingImagem = true;
    this.uploadImagemError = null;

    this.imagemService.uploadImagemEvento(this.eventoId, this.imagemSelecionada).subscribe({
      next: (imagemUrl) => {
        this.imagemAtualUrl = imagemUrl;
        this.limparSelecaoImagem();
        this.isUploadingImagem = false;
        this.showSnackBar('Imagem enviada com sucesso!', 'success');

        // Atualizar evento local
        if (this.evento) {
          this.evento.imagemUrl = imagemUrl;
        }
      },
      error: (error) => {
        console.error('Erro ao fazer upload de imagem:', error);
        this.uploadImagemError = error.error || 'Erro ao enviar imagem';
        this.isUploadingImagem = false;
        this.showSnackBar(this.uploadImagemError || 'Erro ao enviar imagem', 'error');
      }
    });
  }

  /**
   * Remove a imagem do evento
   */
  removerImagem(): void {
    if (!this.eventoId) {
      return;
    }

    if (!confirm('Tem certeza que deseja remover a imagem do evento?')) {
      return;
    }

    this.isUploadingImagem = true;

    this.imagemService.removerImagemEvento(this.eventoId).subscribe({
      next: () => {
        this.imagemAtualUrl = null;
        this.isUploadingImagem = false;
        this.showSnackBar('Imagem removida com sucesso!', 'success');

        // Atualizar evento local
        if (this.evento) {
          this.evento.imagemUrl = undefined;
        }
      },
      error: (error) => {
        console.error('Erro ao remover imagem:', error);
        this.isUploadingImagem = false;
        this.showSnackBar('Erro ao remover imagem', 'error');
      }
    });
  }

  /**
   * Limpa seleção e preview de imagem
   */
  limparSelecaoImagem(): void {
    this.imagemSelecionada = null;
    this.imagemPreview = null;
    this.uploadImagemError = null;

    // Limpar input file
    const inputFile = document.getElementById('imagem-input') as HTMLInputElement;
    if (inputFile) {
      inputFile.value = '';
    }
  }

  /**
   * Retorna URL completa da imagem atual
   */
  getImagemUrl(): string {
    if (!this.imagemAtualUrl) {
      return '';
    }
    return this.imagemService.getImagemUrl(this.imagemAtualUrl);
  }

  /**
   * Abre seletor de arquivo
   */
  abrirSeletorImagem(): void {
    const inputFile = document.getElementById('imagem-input') as HTMLInputElement;
    if (inputFile) {
      inputFile.click();
    }
  }

  // ==================== MÉTODOS DE CATEGORIAS ====================

  /**
   * Inicializa o formulário de categoria com validações
   */
  inicializarFormularioCategoria(): void {
    this.formularioCategoria = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(100)]],
      descricao: ['', Validators.maxLength(300)],
      idadeMinima: [null, [Validators.min(16), Validators.max(80)]],
      idadeMaxima: [null, [Validators.min(16), Validators.max(100)]],
      genero: [null],
      tipoParticipacao: ['INDIVIDUAL', Validators.required],
      quantidadeDeAtletasPorEquipe: [null, [Validators.min(1), Validators.max(6)]],
      valorInscricao: [0, [Validators.required, Validators.min(0)]],
      ativa: [true]
    }, { validators: this.validadorIdadeCategoria });
  }

  /**
   * Validador customizado para verificar se idade máxima >= idade mínima
   */
  validadorIdadeCategoria(control: AbstractControl): ValidationErrors | null {
    const idadeMin = control.get('idadeMinima')?.value;
    const idadeMax = control.get('idadeMaxima')?.value;

    if (idadeMin && idadeMax && idadeMax < idadeMin) {
      return { idadeInvalida: true };
    }

    return null;
  }

  /**
   * Carrega lista de categorias do evento
   */
  carregarCategorias(): void {
    if (!this.eventoId) return;

    this.isLoadingCategorias = true;
    this.categoriaService.listarCategoriasPorEvento(this.eventoId).subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.isLoadingCategorias = false;
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
        this.showSnackBar('Erro ao carregar categorias', 'error');
        this.isLoadingCategorias = false;
      }
    });
  }

  /**
   * Abre formulário para criar nova categoria
   */
  abrirFormularioCategoria(): void {
    this.modoCategoria = 'criar';
    this.categoriaEmEdicao = null;
    this.formularioCategoria.reset({
      tipoParticipacao: 'INDIVIDUAL',
      valorInscricao: 0,
      ativa: true,
      genero: null
    });
    this.mostrarFormularioCategoria = true;
  }

  /**
   * Abre formulário para editar categoria existente
   */
  editarCategoria(categoria: CategoriaSummaryResponse): void {
    this.modoCategoria = 'editar';

    // Buscar categoria completa
    this.categoriaService.buscarCategoriaPorIdCompleta(categoria.id).subscribe({
      next: (categoriaCompleta) => {
        this.categoriaEmEdicao = categoriaCompleta;
        this.formularioCategoria.patchValue({
          nome: categoriaCompleta.nome,
          descricao: categoriaCompleta.descricao || '',
          idadeMinima: categoriaCompleta.idadeMinima,
          idadeMaxima: categoriaCompleta.idadeMaxima,
          genero: categoriaCompleta.genero,
          tipoParticipacao: categoriaCompleta.tipoParticipacao,
          quantidadeDeAtletasPorEquipe: categoriaCompleta.quantidadeDeAtletasPorEquipe,
          valorInscricao: categoriaCompleta.valorInscricao,
          ativa: categoriaCompleta.ativa
        });
        this.mostrarFormularioCategoria = true;
      },
      error: (error) => {
        console.error('Erro ao carregar categoria:', error);
        this.showSnackBar('Erro ao carregar categoria para edição', 'error');
      }
    });
  }

  /**
   * Salva categoria (cria ou atualiza)
   */
  salvarCategoria(): void {
    if (this.formularioCategoria.invalid || !this.eventoId) {
      this.formularioCategoria.markAllAsTouched();
      this.showSnackBar('Por favor, preencha todos os campos obrigatórios', 'error');
      return;
    }

    this.isSavingCategoria = true;
    const formValue = this.formularioCategoria.value;

    const categoriaData: CategoriaCreateRequest = {
      nome: formValue.nome,
      descricao: formValue.descricao || undefined,
      idadeMinima: formValue.idadeMinima || undefined,
      idadeMaxima: formValue.idadeMaxima || undefined,
      genero: formValue.genero || undefined,
      tipoParticipacao: formValue.tipoParticipacao,
      quantidadeDeAtletasPorEquipe: formValue.quantidadeDeAtletasPorEquipe || undefined,
      valorInscricao: formValue.valorInscricao,
      ativa: formValue.ativa
    };

    const operacao = this.modoCategoria === 'criar'
      ? this.categoriaService.criarCategoria(this.eventoId, categoriaData)
      : this.categoriaService.atualizarCategoria(this.categoriaEmEdicao!.id, categoriaData);

    operacao.subscribe({
      next: () => {
        const mensagem = this.modoCategoria === 'criar'
          ? 'Categoria criada com sucesso!'
          : 'Categoria atualizada com sucesso!';

        this.showSnackBar(mensagem, 'success');
        this.carregarCategorias();
        this.cancelarFormularioCategoria();
        this.isSavingCategoria = false;
      },
      error: (error) => {
        console.error('Erro ao salvar categoria:', error);
        const mensagem = error.error?.message || 'Erro ao salvar categoria';
        this.showSnackBar(mensagem, 'error');
        this.isSavingCategoria = false;
      }
    });
  }

  /**
   * Cancela formulário de categoria
   */
  cancelarFormularioCategoria(): void {
    this.mostrarFormularioCategoria = false;
    this.categoriaEmEdicao = null;
    this.formularioCategoria.reset({
      tipoParticipacao: 'INDIVIDUAL',
      valorInscricao: 0,
      ativa: true,
      genero: null
    });
  }

  /**
   * Deleta categoria com confirmação
   */
  deletarCategoria(categoriaId: number): void {
    const confirmacao = confirm('Tem certeza que deseja deletar esta categoria? Esta ação não pode ser desfeita.');

    if (!confirmacao) return;

    this.categoriaService.deletarCategoria(categoriaId).subscribe({
      next: () => {
        this.showSnackBar('Categoria deletada com sucesso!', 'success');
        this.carregarCategorias();
      },
      error: (error) => {
        console.error('Erro ao deletar categoria:', error);
        const mensagem = error.error?.message || 'Erro ao deletar categoria';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  /**
   * Alterna status ativo/inativo da categoria
   */
  toggleStatusCategoria(categoria: CategoriaSummaryResponse): void {
    const operacao = categoria.ativa
      ? this.categoriaService.desativarCategoria(categoria.id)
      : this.categoriaService.ativarCategoria(categoria.id);

    operacao.subscribe({
      next: () => {
        const mensagem = categoria.ativa
          ? 'Categoria desativada com sucesso!'
          : 'Categoria ativada com sucesso!';

        this.showSnackBar(mensagem, 'success');
        this.carregarCategorias();
      },
      error: (error) => {
        console.error('Erro ao alterar status da categoria:', error);
        const mensagem = error.error?.message || 'Erro ao alterar status da categoria';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  /**
   * Retorna label formatado do gênero
   */
  getGeneroLabel(genero: Genero | null | undefined): string {
    return getGeneroLabel(genero);
  }

  /**
   * Retorna label formatado do tipo de participação
   */
  getTipoLabel(tipo: TipoParticipacao): string {
    return getTipoParticipacaoLabel(tipo);
  }

  /**
   * Retorna label formatado da faixa etária
   */
  getFaixaEtariaLabel(idadeMin?: number, idadeMax?: number): string {
    return formatarFaixaEtaria(idadeMin, idadeMax);
  }

  // ==================== MÉTODOS DE WORKOUTS ====================

  /**
   * Inicializa o formulário de workout com validações
   */
  inicializarFormularioWorkout(): void {
    this.formularioWorkout = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(100)]],
      descricao: ['', Validators.maxLength(1000)],
      tipo: ['REPS', Validators.required],
      categoriasIds: [[], Validators.required],
      ativo: [true]
    });
  }

  /**
   * Carrega lista de workouts do evento
   */
  carregarWorkouts(): void {
    if (!this.eventoId) return;

    this.isLoadingWorkouts = true;
    this.workoutService.buscarWorkoutsPorEvento(this.eventoId).subscribe({
      next: (workouts) => {
        this.workouts = workouts;
        this.isLoadingWorkouts = false;
      },
      error: (error) => {
        console.error('Erro ao carregar workouts:', error);
        this.showSnackBar('Erro ao carregar workouts', 'error');
        this.isLoadingWorkouts = false;
      }
    });
  }

  /**
   * Abre formulário para criar novo workout
   */
  abrirFormularioWorkout(): void {
    this.modoWorkout = 'criar';
    this.workoutEmEdicao = null;
    this.formularioWorkout.reset({
      tipo: 'REPS',
      categoriasIds: [],
      ativo: true
    });
    this.mostrarFormularioWorkout = true;
  }

  /**
   * Abre formulário para editar workout existente
   */
  editarWorkout(workout: Workout): void {
    this.modoWorkout = 'editar';

    // Buscar workout completo
    this.workoutService.buscarWorkoutPorId(workout.id).subscribe({
      next: (workoutCompleto) => {
        this.workoutEmEdicao = workoutCompleto;
        this.formularioWorkout.patchValue({
          nome: workoutCompleto.nome,
          descricao: workoutCompleto.descricao || '',
          tipo: workoutCompleto.tipo,
          categoriasIds: workoutCompleto.categorias.map(c => c.id),
          ativo: workoutCompleto.ativo
        });
        this.mostrarFormularioWorkout = true;
      },
      error: (error) => {
        console.error('Erro ao carregar workout:', error);
        this.showSnackBar('Erro ao carregar workout para edição', 'error');
      }
    });
  }

  /**
   * Salva workout (cria ou atualiza)
   */
  salvarWorkout(): void {
    if (this.formularioWorkout.invalid || !this.eventoId) {
      this.formularioWorkout.markAllAsTouched();
      this.showSnackBar('Por favor, preencha todos os campos obrigatórios', 'error');
      return;
    }

    const formValue = this.formularioWorkout.value;

    // Validar se há categorias selecionadas
    if (!formValue.categoriasIds || formValue.categoriasIds.length === 0) {
      this.showSnackBar('Selecione pelo menos uma categoria', 'error');
      return;
    }

    this.isSavingWorkout = true;

    if (this.modoWorkout === 'criar') {
      const workoutData: WorkoutCreateRequest = {
        nome: formValue.nome,
        descricao: formValue.descricao || undefined,
        tipo: formValue.tipo,
        eventoId: this.eventoId,
        categoriasIds: formValue.categoriasIds,
        ativo: formValue.ativo
      };

      this.workoutService.criarWorkout(workoutData).subscribe({
        next: () => {
          this.showSnackBar('Workout criado com sucesso!', 'success');
          this.carregarWorkouts();
          this.cancelarFormularioWorkout();
          this.isSavingWorkout = false;
        },
        error: (error) => {
          console.error('Erro ao criar workout:', error);
          const mensagem = error.error?.message || 'Erro ao criar workout';
          this.showSnackBar(mensagem, 'error');
          this.isSavingWorkout = false;
        }
      });
    } else {
      const workoutData: WorkoutUpdateRequest = {
        nome: formValue.nome,
        descricao: formValue.descricao || undefined,
        tipo: formValue.tipo,
        categoriasIds: formValue.categoriasIds,
        ativo: formValue.ativo
      };

      this.workoutService.atualizarWorkout(this.workoutEmEdicao!.id, workoutData).subscribe({
        next: () => {
          this.showSnackBar('Workout atualizado com sucesso!', 'success');
          this.carregarWorkouts();
          this.cancelarFormularioWorkout();
          this.isSavingWorkout = false;
        },
        error: (error) => {
          console.error('Erro ao atualizar workout:', error);
          const mensagem = error.error?.message || 'Erro ao atualizar workout';
          this.showSnackBar(mensagem, 'error');
          this.isSavingWorkout = false;
        }
      });
    }
  }

  /**
   * Cancela formulário de workout
   */
  cancelarFormularioWorkout(): void {
    this.mostrarFormularioWorkout = false;
    this.workoutEmEdicao = null;
    this.formularioWorkout.reset({
      tipo: 'REPS',
      categoriasIds: [],
      ativo: true
    });
  }

  /**
   * Deleta workout com confirmação
   */
  deletarWorkout(workoutId: number): void {
    const confirmacao = confirm('Tem certeza que deseja deletar este workout? Esta ação não pode ser desfeita.');

    if (!confirmacao) return;

    this.workoutService.deletarWorkout(workoutId).subscribe({
      next: () => {
        this.showSnackBar('Workout deletado com sucesso!', 'success');
        this.carregarWorkouts();
      },
      error: (error) => {
        console.error('Erro ao deletar workout:', error);
        const mensagem = error.error?.message || 'Erro ao deletar workout';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  /**
   * Alterna status ativo/inativo do workout
   */
  toggleStatusWorkout(workout: Workout): void {
    const operacao = workout.ativo
      ? this.workoutService.desativarWorkout(workout.id)
      : this.workoutService.ativarWorkout(workout.id);

    operacao.subscribe({
      next: () => {
        const mensagem = workout.ativo
          ? 'Workout desativado com sucesso!'
          : 'Workout ativado com sucesso!';

        this.showSnackBar(mensagem, 'success');
        this.carregarWorkouts();
      },
      error: (error) => {
        console.error('Erro ao alterar status do workout:', error);
        const mensagem = error.error?.message || 'Erro ao alterar status do workout';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  /**
   * Retorna label formatado do tipo de workout
   */
  getTipoWorkoutLabel(tipo: WorkoutType | string): string {
    return getTipoWorkoutLabel(tipo);
  }

  /**
   * Retorna label formatado da unidade de medida
   */
  getUnidadeMedidaLabel(tipo: WorkoutType | string): string {
    return getUnidadeMedidaLabel(tipo);
  }

  /**
   * Retorna categorias ativas para seleção no formulário de workout
   */
  getCategoriasAtivas(): CategoriaSummaryResponse[] {
    return this.categorias.filter(cat => cat.ativa);
  }

  // ==================== MÉTODOS DE TIMELINE ====================

  /**
   * Inicializa o formulário de timeline com validações
   */
  inicializarFormularioTimeline(): void {
    this.formularioTimeline = this.fb.group({
      descricaoDiaUm: ['', [Validators.maxLength(5000)]],
      descricaoDiaDois: ['', [Validators.maxLength(5000)]],
      descricaoDiaTres: ['', [Validators.maxLength(5000)]],
      descricaoDiaQuatro: ['', [Validators.maxLength(5000)]]
    });
  }

  /**
   * Carrega timeline do evento
   */
  carregarTimeline(): void {
    if (!this.eventoId) return;

    this.isLoadingTimeline = true;
    this.timelineService.buscarTimelinePorEvento(this.eventoId).subscribe({
      next: (timeline) => {
        this.timeline = timeline;
        // Verifica se a timeline realmente existe (tem ID) ou é apenas um objeto vazio do backend
        this.timelineExists = timeline.id !== null && timeline.id !== undefined;
        if (this.timelineExists) {
          this.preencherFormularioTimeline(timeline);
        }
        this.isLoadingTimeline = false;
      },
      error: (error) => {
        // Se erro 404, timeline não existe ainda
        if (error.status === 404) {
          this.timelineExists = false;
          this.timeline = null;
        } else {
          console.error('Erro ao carregar timeline:', error);
        }
        this.isLoadingTimeline = false;
      }
    });
  }

  /**
   * Preenche formulário com dados da timeline
   */
  preencherFormularioTimeline(timeline: Timeline): void {
    this.formularioTimeline.patchValue({
      descricaoDiaUm: timeline.descricaoDiaUm || '',
      descricaoDiaDois: timeline.descricaoDiaDois || '',
      descricaoDiaTres: timeline.descricaoDiaTres || '',
      descricaoDiaQuatro: timeline.descricaoDiaQuatro || ''
    });
  }

  /**
   * Salva timeline (cria ou atualiza)
   */
  salvarTimeline(): void {
    if (this.formularioTimeline.invalid || !this.eventoId) {
      this.formularioTimeline.markAllAsTouched();
      this.showSnackBar('Por favor, verifique os campos', 'error');
      return;
    }

    console.log('[DEBUG] timelineExists:', this.timelineExists);
    console.log('[DEBUG] timeline:', this.timeline);

    this.isSavingTimeline = true;
    const formValue = this.formularioTimeline.value;

    const timelineData: TimelineCreateRequest = {
      descricaoDiaUm: formValue.descricaoDiaUm || null,
      descricaoDiaDois: formValue.descricaoDiaDois || null,
      descricaoDiaTres: formValue.descricaoDiaTres || null,
      descricaoDiaQuatro: formValue.descricaoDiaQuatro || null
    };

    const operacao = this.timelineExists
      ? this.timelineService.atualizarTimeline(this.eventoId, timelineData)
      : this.timelineService.criarTimeline(this.eventoId, timelineData);

    operacao.subscribe({
      next: (timeline) => {
        this.timeline = timeline;
        this.timelineExists = true;
        const mensagem = timeline.id && timeline.vazia === false
          ? 'Timeline atualizada com sucesso!'
          : 'Timeline criada com sucesso!';
        this.showSnackBar(mensagem, 'success');
        this.isSavingTimeline = false;
      },
      error: (error) => {
        console.error('Erro ao salvar timeline:', error);
        const mensagem = error.error?.message || 'Erro ao salvar timeline';
        this.showSnackBar(mensagem, 'error');
        this.isSavingTimeline = false;
      }
    });
  }

  /**
   * Limpa formulário de timeline
   */
  limparFormularioTimeline(): void {
    this.formularioTimeline.reset({
      descricaoDiaUm: '',
      descricaoDiaDois: '',
      descricaoDiaTres: '',
      descricaoDiaQuatro: ''
    });
  }

  /**
   * Retorna número de dias com descrição preenchida
   */
  getDiasPreenchidos(): number {
    if (!this.timeline) return 0;
    return this.timeline.totalDiasComDescricao || 0;
  }

  /**
   * Verifica se um dia específico está preenchido
   */
  diaEstaPreenchido(dia: number): boolean {
    if (!this.timeline) return false;

    switch (dia) {
      case 1: return this.timeline.temDescricaoDiaUm;
      case 2: return this.timeline.temDescricaoDiaDois;
      case 3: return this.timeline.temDescricaoDiaTres;
      case 4: return this.timeline.temDescricaoDiaQuatro;
      default: return false;
    }
  }

  // ==================== MÉTODOS DE ANEXOS ====================

  /**
   * Carrega anexos do evento
   */
  carregarAnexos(): void {
    if (!this.eventoId) return;

    this.isLoadingAnexos = true;
    this.anexoService.buscarAnexosPorEvento(this.eventoId).subscribe({
      next: (anexos) => {
        this.anexos = anexos;
        this.isLoadingAnexos = false;
      },
      error: (error) => {
        console.error('Erro ao carregar anexos:', error);
        this.isLoadingAnexos = false;
        this.showSnackBar('Erro ao carregar anexos', 'error');
      }
    });
  }

  /**
   * Valida arquivo antes do upload
   */
  validarArquivo(arquivo: File): boolean {
    const tiposPermitidos = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    const tamanhoMaximo = 10 * 1024 * 1024; // 10 MB

    if (!tiposPermitidos.includes(arquivo.type)) {
      this.showSnackBar('Tipo de arquivo não permitido. Use PDF ou imagens (JPG, PNG, GIF)', 'error');
      return false;
    }

    if (arquivo.size > tamanhoMaximo) {
      this.showSnackBar('Arquivo muito grande. Tamanho máximo: 10 MB', 'error');
      return false;
    }

    return true;
  }

  /**
   * Processa seleção de arquivos
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadArquivos(Array.from(input.files));
      input.value = '';
    }
  }

  /**
   * Processa drag over
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  /**
   * Processa drag leave
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  /**
   * Processa drop de arquivos
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadArquivos(Array.from(files));
    }
  }

  /**
   * Faz upload de múltiplos arquivos
   */
  uploadArquivos(arquivos: File[]): void {
    if (!this.eventoId) return;

    const arquivosValidos = arquivos.filter(arquivo => this.validarArquivo(arquivo));

    if (arquivosValidos.length === 0) return;

    this.isUploading = true;
    let uploadsConcluidos = 0;

    arquivosValidos.forEach(arquivo => {
      this.anexoService.uploadAnexo(arquivo, this.eventoId!).subscribe({
        next: () => {
          uploadsConcluidos++;
          if (uploadsConcluidos === arquivosValidos.length) {
            this.isUploading = false;
            this.showSnackBar(`${arquivosValidos.length} arquivo(s) enviado(s) com sucesso!`, 'success');
            this.carregarAnexos();
          }
        },
        error: (error) => {
          console.error('Erro ao fazer upload:', error);
          this.isUploading = false;
          const mensagem = error.error?.message || 'Erro ao fazer upload do arquivo';
          this.showSnackBar(mensagem, 'error');
        }
      });
    });
  }

  /**
   * Faz download de um anexo
   */
  downloadAnexo(anexo: Anexo): void {
    this.anexoService.downloadAnexo(anexo.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = anexo.nomeArquivo;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erro ao fazer download:', error);
        this.showSnackBar('Erro ao fazer download do arquivo', 'error');
      }
    });
  }

  /**
   * Remove um anexo
   */
  removerAnexo(anexo: Anexo): void {
    const confirmacao = confirm(`Tem certeza que deseja remover o arquivo "${anexo.nomeArquivo}"?`);

    if (!confirmacao) return;

    this.anexoService.removerAnexo(anexo.id).subscribe({
      next: () => {
        this.showSnackBar('Arquivo removido com sucesso!', 'success');
        this.carregarAnexos();
      },
      error: (error) => {
        console.error('Erro ao remover anexo:', error);
        const mensagem = error.error?.message || 'Erro ao remover arquivo';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  // ==================== MÉTODOS DE INSCRIÇÕES ====================

  /**
   * Carrega inscrições do evento
   */
  carregarInscricoes(): void {
    if (!this.eventoId) return;

    this.isLoadingInscricoes = true;
    this.inscricaoService.buscarPorEvento(this.eventoId).subscribe({
      next: (inscricoes) => {
        this.inscricoes = inscricoes;
        this.inscricoesFiltradas = inscricoes;
        this.calcularEstatisticasInscricoes();
        this.isLoadingInscricoes = false;
      },
      error: (error) => {
        console.error('Erro ao carregar inscrições:', error);
        this.isLoadingInscricoes = false;
        this.showSnackBar('Erro ao carregar inscrições', 'error');
      }
    });
  }

  /**
   * Calcula estatísticas das inscrições
   */
  calcularEstatisticasInscricoes(): void {
    this.estatisticasInscricoes = {
      total: this.inscricoes.length,
      confirmadas: this.inscricoes.filter(i => i.status === 'CONFIRMADA').length,
      pendentes: this.inscricoes.filter(i => i.status === 'PENDENTE').length,
      canceladas: this.inscricoes.filter(i => i.status === 'CANCELADA').length
    };
  }

  /**
   * Aplica filtros nas inscrições
   */
  aplicarFiltrosInscricoes(): void {
    this.inscricoesFiltradas = this.inscricoes.filter(inscricao => {
      // Filtro por status
      if (this.filtrosInscricoes.status && inscricao.status !== this.filtrosInscricoes.status) {
        return false;
      }

      // Filtro por categoria
      if (this.filtrosInscricoes.categoriaId && inscricao.nomeCategoria) {
        const categoria = this.categorias.find(c => c.id === this.filtrosInscricoes.categoriaId);
        if (categoria && inscricao.nomeCategoria !== categoria.nome) {
          return false;
        }
      }

      // Filtro por nome do participante
      if (this.filtrosInscricoes.nomeParticipante) {
        const nome = this.filtrosInscricoes.nomeParticipante.toLowerCase();
        return inscricao.nomeParticipante.toLowerCase().includes(nome);
      }

      return true;
    });
  }

  /**
   * Limpa todos os filtros
   */
  limparFiltrosInscricoes(): void {
    this.filtrosInscricoes = {
      status: null,
      categoriaId: null,
      nomeParticipante: ''
    };
    this.inscricoesFiltradas = this.inscricoes;
  }

  /**
   * Abre detalhes da inscrição (TODO: implementar modal)
   */
  abrirDetalhesInscricao(inscricao: InscricaoSummaryResponse): void {
    console.log('Detalhes da inscrição:', inscricao);
    this.showSnackBar(`Visualizando detalhes da inscrição #${inscricao.id}`, 'success');
  }

  /**
   * Confirma uma inscrição
   */
  confirmarInscricao(inscricaoId: number): void {
    const confirmacao = confirm('Tem certeza que deseja confirmar esta inscrição?');
    if (!confirmacao) return;

    this.inscricaoService.confirmar(inscricaoId).subscribe({
      next: () => {
        this.showSnackBar('Inscrição confirmada com sucesso!', 'success');
        this.carregarInscricoes();
      },
      error: (error) => {
        console.error('Erro ao confirmar inscrição:', error);
        const mensagem = error.error?.message || 'Erro ao confirmar inscrição';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  /**
   * Cancela uma inscrição
   */
  cancelarInscricao(inscricaoId: number): void {
    const motivo = prompt('Informe o motivo do cancelamento:');
    if (!motivo) return;

    this.inscricaoService.cancelar(inscricaoId, motivo).subscribe({
      next: () => {
        this.showSnackBar('Inscrição cancelada com sucesso!', 'success');
        this.carregarInscricoes();
      },
      error: (error) => {
        console.error('Erro ao cancelar inscrição:', error);
        const mensagem = error.error?.message || 'Erro ao cancelar inscrição';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }

  /**
   * Retorna classe CSS para o status
   */
  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      'PENDENTE': 'status-pendente',
      'CONFIRMADA': 'status-confirmada',
      'CANCELADA': 'status-cancelada',
      'RECUSADA': 'status-recusada',
      'EXPIRADA': 'status-expirada',
      'LISTA_ESPERA': 'status-lista-espera'
    };
    return classes[status] || '';
  }

  /**
   * Abre dialog para criar nova inscrição
   */
  abrirDialogNovaInscricao(): void {
    if (!this.eventoId) {
      this.showSnackBar('Evento não identificado', 'error');
      return;
    }

    const dialogRef = this.dialog.open(InscricaoFormDialogComponent, {
      width: '600px',
      disableClose: false,
      data: {
        eventoId: this.eventoId
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.success) {
        this.showSnackBar('Inscrição criada com sucesso!', 'success');
        this.carregarInscricoes();
      } else if (result?.error) {
        const mensagem = result.error?.error?.message || 'Erro ao criar inscrição';
        this.showSnackBar(mensagem, 'error');
      }
    });
  }
}
