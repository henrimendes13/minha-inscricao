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
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';

import { EventoService } from '../../../core/services/evento.service';
import { ImagemService } from '../../../core/services/imagem.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { WorkoutService } from '../../../core/services/workout.service';
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

@Component({
  selector: 'app-evento-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
    MatExpansionModule,
    MatChipsModule,
    MatTooltipModule,
    MatCheckboxModule
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

    if (this.modo === 'editar' && this.eventoId) {
      this.carregarEvento();
      this.carregarCategorias();
      this.carregarWorkouts();
    }
  }

  detectarModo(): void {
    // Verifica se existe :id na rota para determinar modo
    this.eventoId = this.route.snapshot.params['id'];

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
}
