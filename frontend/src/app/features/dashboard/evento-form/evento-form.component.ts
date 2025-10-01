import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
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

import { EventoService } from '../../../core/services/evento.service';
import { ImagemService } from '../../../core/services/imagem.service';
import { EventoApiResponse, EventoCreateRequest, EventoUpdateRequest } from '../../../models/evento.model';

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
    MatProgressSpinnerModule
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

  estadosBrasileiros = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  constructor(
    private fb: FormBuilder,
    private eventoService: EventoService,
    private imagemService: ImagemService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.detectarModo();
    this.inicializarFormulario();

    if (this.modo === 'editar' && this.eventoId) {
      this.carregarEvento();
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
      this.showSnackBar(this.uploadImagemError, 'error');
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
}
