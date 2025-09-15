import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ActivatedRoute, Router } from '@angular/router';

import { EventoService } from '../../../core/services/evento.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { InscricaoService } from '../../../core/services/inscricao.service';
import { AuthService } from '../../../core/auth/auth.service';

import { EventoApiResponse } from '../../../models/evento.model';
import { CategoriaInscricao } from '../../../models/leaderboard.model';
import { 
  AtletaInscricaoDTO, 
  EquipeInscricaoDTO, 
  CategoriaEscolhida, 
  TipoParticipacao
} from '../../../models/inscricao.model';
import { Genero, AtletaResponseDTO } from '../../../models/atleta.model';
import { EquipeResponseDTO } from '../../../models/equipe.model';

@Component({
  selector: 'app-inscricao-nova',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatStepperModule,
    MatCheckboxModule
  ],
  templateUrl: './inscricao-nova.component.html',
  styleUrl: './inscricao-nova.component.scss'
})
export class InscricaoNovaComponent implements OnInit {
  // Estados do componente
  isLoading = true;
  isSubmitting = false;
  hasError = false;
  
  // Dados do evento e categorias
  evento: EventoApiResponse | null = null;
  categorias: CategoriaInscricao[] = [];
  categoriasEscolhidas: CategoriaEscolhida[] = [];
  tipoInscricao: TipoParticipacao = TipoParticipacao.INDIVIDUAL;
  valorTotal = 0;
  
  // Formulários
  inscricaoForm: FormGroup;
  
  // Configurações
  generos = [
    { value: Genero.MASCULINO, label: 'Masculino' },
    { value: Genero.FEMININO, label: 'Feminino' }
  ];
  
  eventoId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private eventoService: EventoService,
    private categoriaService: CategoriaService,
    private inscricaoService: InscricaoService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.inscricaoForm = this.criarFormularioVazio();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.eventoId = +params['eventoId'];
      if (this.eventoId) {
        this.carregarDadosIniciais();
      } else {
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  private async carregarDadosIniciais(): Promise<void> {
    try {
      this.isLoading = true;
      
      // Buscar parâmetros da URL
      this.route.queryParams.subscribe(params => {
        if (params['categorias']) {
          this.categoriasEscolhidas = JSON.parse(params['categorias']);
        }
      });
      
      // Carregar evento
      const eventoResponse = await this.eventoService.buscarEventoPorId(this.eventoId).toPromise();
      if (!eventoResponse) {
        throw new Error('Evento não encontrado');
      }
      this.evento = eventoResponse ?? null;
      
      // Carregar categorias
      await this.carregarCategorias();
      
      // Determinar tipo de inscrição
      this.determinarTipoInscricao();
      
      // Calcular valor total
      this.calcularValorTotal();
      
      // Criar formulário baseado no tipo
      this.criarFormulario();
      
      this.isLoading = false;
      
    } catch (error) {
      console.error('Erro ao carregar dados iniciais:', error);
      this.hasError = true;
      this.isLoading = false;
      this.snackBar.open('Erro ao carregar dados. Tente novamente.', 'Fechar', { duration: 5000 });
    }
  }

  private async carregarCategorias(): Promise<void> {
    const todasCategorias = await this.categoriaService.buscarCategoriasPorEvento(this.eventoId).toPromise();
    
    // Filtrar apenas as categorias selecionadas
    const idsEscolhidos = this.categoriasEscolhidas.map(c => c.categoriaId);
    this.categorias = todasCategorias?.filter(cat => idsEscolhidos.includes(cat.id)) || [];
  }

  private determinarTipoInscricao(): void {
    const tiposParticipacao = this.categorias.map(c => c.tipoParticipacao);
    const tiposUnicos = [...new Set(tiposParticipacao)];
    
    if (tiposUnicos.length > 1) {
      throw new Error('Não é possível misturar categorias individuais e de equipe');
    }
    
    this.tipoInscricao = tiposUnicos[0] === 'EQUIPE' ? TipoParticipacao.EQUIPE : TipoParticipacao.INDIVIDUAL;
  }

  private calcularValorTotal(): void {
    this.valorTotal = this.categoriasEscolhidas.reduce((total, escolha) => {
      const categoria = this.categorias.find(c => c.id === escolha.categoriaId);
      return total + (categoria ? categoria.valorInscricao * escolha.quantidade : 0);
    }, 0);
  }

  private criarFormularioVazio(): FormGroup {
    return this.fb.group({});
  }

  private criarFormulario(): void {
    if (this.tipoInscricao === TipoParticipacao.INDIVIDUAL) {
      this.criarFormularioIndividual();
    } else {
      this.criarFormularioEquipe();
    }
  }

  private criarFormularioIndividual(): void {
    const atletasControls: FormGroup[] = [];
    
    // Criar um formulário de atleta para cada categoria/quantidade
    this.categoriasEscolhidas.forEach(escolha => {
      const categoria = this.categorias.find(c => c.id === escolha.categoriaId);
      
      for (let i = 0; i < escolha.quantidade; i++) {
        atletasControls.push(this.criarFormularioAtleta(categoria));
      }
    });
    
    const atletasArray = this.fb.array(atletasControls);
    
    this.inscricaoForm = this.fb.group({
      atletas: atletasArray,
      termosGerais: [false, Validators.requiredTrue]
    });
  }

  private criarFormularioEquipe(): void {
    this.inscricaoForm = this.fb.group({
      nomeEquipe: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      categoriaId: [this.categoriasEscolhidas[0]?.categoriaId || null, Validators.required],
      atletas: this.fb.array([
        this.criarFormularioAtleta(), // Mínimo 1 atleta
        this.criarFormularioAtleta()  // Mínimo 2 atletas
      ]),
      capitaoCpf: [''],
      codigoDesconto: [''],
      termosAceitos: [false, Validators.requiredTrue]
    });
  }

  private criarFormularioAtleta(categoria?: CategoriaInscricao): FormGroup {
    return this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(200)]],
      cpf: ['', [this.validarCpfFormat]],
      dataNascimento: ['', Validators.required],
      genero: [categoria?.genero || '', Validators.required],
      telefone: ['', [this.validarTelefoneFormat]],
      email: ['', [Validators.email, Validators.maxLength(150)]],
      endereco: ['', Validators.maxLength(200)],
      emergenciaNome: ['', Validators.maxLength(100)],
      emergenciaTelefone: ['', [this.validarTelefoneFormat]],
      observacoesMedicas: ['', Validators.maxLength(500)],
      aceitaTermos: [false, Validators.requiredTrue],
      categoriaId: [categoria?.id || this.categoriasEscolhidas[0]?.categoriaId],
      termosInscricaoAceitos: [false, Validators.requiredTrue]
    });
  }

  // Getters para formulários
  get atletasFormArray(): FormArray {
    return this.inscricaoForm.get('atletas') as FormArray;
  }

  get isFormularioEquipe(): boolean {
    return this.tipoInscricao === TipoParticipacao.EQUIPE;
  }

  // Métodos de validação customizada
  validarCpfFormat = (control: any) => {
    if (!control.value) return null;
    return this.inscricaoService.validarCpf(control.value) ? null : { cpfInvalido: true };
  };

  validarTelefoneFormat = (control: any) => {
    if (!control.value) return null;
    return this.inscricaoService.validarTelefone(control.value) ? null : { telefoneInvalido: true };
  };

  // Métodos de manipulação do formulário de equipe
  adicionarAtleta(): void {
    if (this.atletasFormArray.length < 6) {
      this.atletasFormArray.push(this.criarFormularioAtleta());
    }
  }

  removerAtleta(index: number): void {
    if (this.atletasFormArray.length > 2) {
      this.atletasFormArray.removeAt(index);
    }
  }

  // Submissão do formulário
  async submitInscricao(): Promise<void> {
    if (!this.inscricaoForm.valid) {
      this.marcarCamposComoTocados();
      this.snackBar.open('Por favor, corrija os erros no formulário', 'Fechar', { duration: 5000 });
      return;
    }

    try {
      this.isSubmitting = true;

      if (this.tipoInscricao === TipoParticipacao.INDIVIDUAL) {
        await this.submitInscricaoIndividual();
      } else {
        await this.submitInscricaoEquipe();
      }

      this.snackBar.open('Inscrição realizada com sucesso!', 'Fechar', { duration: 5000 });
      this.router.navigate(['/eventos', this.eventoId]);
      
    } catch (error: any) {
      console.error('Erro ao submeter inscrição:', error);
      const mensagem = error.error?.message || 'Erro ao processar inscrição. Tente novamente.';
      this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
    } finally {
      this.isSubmitting = false;
    }
  }

  private async submitInscricaoIndividual(): Promise<void> {
    const atletas = this.atletasFormArray.value as AtletaInscricaoDTO[];
    
    for (const atleta of atletas) {
      await this.inscricaoService.criarInscricaoIndividual(this.eventoId, atleta).toPromise();
    }
  }

  private async submitInscricaoEquipe(): Promise<void> {
    const formValue = this.inscricaoForm.value;
    
    const equipeData: EquipeInscricaoDTO = {
      nome: formValue.nomeEquipe,
      categoriaId: formValue.categoriaId,
      atletas: formValue.atletas,
      capitaoCpf: formValue.capitaoCpf,
      codigoDesconto: formValue.codigoDesconto,
      termosAceitos: formValue.termosAceitos
    };
    
    await this.inscricaoService.criarInscricaoEquipe(this.eventoId, equipeData).toPromise();
  }

  private marcarCamposComoTocados(): void {
    this.inscricaoForm.markAllAsTouched();
    
    if (this.atletasFormArray) {
      this.atletasFormArray.controls.forEach(control => {
        control.markAllAsTouched();
      });
    }
  }

  // Métodos de formatação
  formatarCpf(event: any, control: any): void {
    const cpfFormatado = this.inscricaoService.formatarCpf(event.target.value);
    control.setValue(cpfFormatado);
  }

  formatarTelefone(event: any, control: any): void {
    const telefoneFormatado = this.inscricaoService.formatarTelefone(event.target.value);
    control.setValue(telefoneFormatado);
  }

  // Navegação
  voltar(): void {
    this.router.navigate(['/eventos', this.eventoId]);
  }

  // Getters para template
  get nomeEvento(): string {
    return this.evento?.nome || 'Evento';
  }

  get resumoCategorias(): string {
    return this.categorias.map(c => c.nome).join(', ');
  }

  getCategoriaById(id: number): CategoriaInscricao | undefined {
    return this.categorias.find(c => c.id === id);
  }
}