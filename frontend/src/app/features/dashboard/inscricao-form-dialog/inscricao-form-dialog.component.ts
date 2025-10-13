import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, FormsModule, AbstractControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

import { InscricaoService } from '../../../core/services/inscricao.service';
import { CategoriaService } from '../../../core/services/categoria.service';

import { InscricaoSimplificadaCreateRequest } from '../../../models/inscricao.model';

interface DialogData {
  eventoId: number;
}

@Component({
  selector: 'app-inscricao-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule,
    MatRadioModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './inscricao-form-dialog.component.html',
  styleUrls: ['./inscricao-form-dialog.component.scss']
})
export class InscricaoFormDialogComponent implements OnInit {
  inscricaoForm!: FormGroup;
  isSubmitting = false;
  isLoadingData = true;

  // Dados para dropdowns (apenas categorias mantém dropdown)
  categorias: any[] = [];
  categoriaSelecionada: any = null;

  // Tipo de inscrição
  tipoInscricao: 'individual' | 'equipe' = 'individual';

  // Controle de atletas da equipe
  get atletasEquipe(): FormArray {
    return this.inscricaoForm.get('atletasEquipe') as FormArray;
  }

  // Quantidade de atletas requerida pela categoria
  get quantidadeAtletasRequerida(): number {
    return this.categoriaSelecionada?.quantidadeDeAtletasPorEquipe || 2;
  }

  // Verificar se pode adicionar atleta
  get podeAdicionarAtleta(): boolean {
    if (!this.categoriaSelecionada) return this.atletasEquipe.length < 6;
    return this.atletasEquipe.length < this.quantidadeAtletasRequerida;
  }

  // Verificar se pode remover atleta
  get podeRemoverAtleta(): boolean {
    if (!this.categoriaSelecionada) return this.atletasEquipe.length > 2;
    return this.atletasEquipe.length > this.quantidadeAtletasRequerida;
  }

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<InscricaoFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private inscricaoService: InscricaoService,
    private categoriaService: CategoriaService
  ) {}

  ngOnInit(): void {
    this.createForm();
    this.loadDropdownData();
  }

  createForm(): void {
    this.inscricaoForm = this.fb.group({
      // Campos de texto livre
      usuarioEmail: ['', [Validators.required, Validators.email]],
      atletaNome: [''],

      // Campo de equipe
      nomeEquipe: [''],
      atletasEquipe: this.fb.array([]),

      // Demais campos
      categoriaId: [null, Validators.required],
      valor: [0, [Validators.required, Validators.min(0)]],
      codigoDesconto: [''],
      valorDesconto: [0, Validators.min(0)],
      termosAceitos: [false, Validators.requiredTrue],
      observacoes: ['']
    });

    // Inicializar validações para individual
    this.inscricaoForm.get('atletaNome')?.setValidators([Validators.required]);
    this.inscricaoForm.get('atletaNome')?.updateValueAndValidity();
  }

  loadDropdownData(): void {
    this.isLoadingData = true;

    // Apenas carregar categorias
    this.categoriaService.buscarPorEvento(this.data.eventoId).subscribe({
      next: (categorias) => {
        this.categorias = categorias || [];
        this.isLoadingData = false;
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
        this.isLoadingData = false;
      }
    });
  }

  getCategoriasFiltradasPorTipo(): any[] {
    if (!this.categorias || this.categorias.length === 0) {
      return [];
    }

    const tipoFiltro = this.tipoInscricao === 'individual' ? 'INDIVIDUAL' : 'EQUIPE';
    return this.categorias.filter(cat => cat.tipoParticipacao === tipoFiltro);
  }

  onCategoriaChange(categoriaId: number): void {
    // Buscar categoria selecionada
    this.categoriaSelecionada = this.categorias.find(cat => cat.id === categoriaId);

    // Se for inscrição de equipe, ajustar quantidade de atletas
    if (this.tipoInscricao === 'equipe' && this.categoriaSelecionada) {
      const quantidadeRequerida = this.categoriaSelecionada.quantidadeDeAtletasPorEquipe || 2;

      // Ajustar quantidade de atletas no FormArray
      const atletasAtuais = this.atletasEquipe.length;

      if (atletasAtuais < quantidadeRequerida) {
        // Adicionar atletas faltantes
        for (let i = atletasAtuais; i < quantidadeRequerida; i++) {
          this.adicionarAtletaEquipe();
        }
      } else if (atletasAtuais > quantidadeRequerida) {
        // Remover atletas excedentes
        while (this.atletasEquipe.length > quantidadeRequerida) {
          this.atletasEquipe.removeAt(this.atletasEquipe.length - 1);
        }
      }

      // Definir o primeiro atleta como capitão se ainda não houver capitão
      if (!this.temCapitao() && this.atletasEquipe.length > 0) {
        this.setCapitao(0);
      }
    }
  }

  onTipoInscricaoChange(tipo: 'individual' | 'equipe'): void {
    this.tipoInscricao = tipo;

    // Limpar categoria selecionada ao mudar tipo
    this.categoriaSelecionada = null;
    this.inscricaoForm.patchValue({ categoriaId: null });

    if (tipo === 'individual') {
      // Modo individual: validar atleta nome
      this.inscricaoForm.get('atletaNome')?.setValidators([Validators.required]);
      this.inscricaoForm.get('nomeEquipe')?.clearValidators();
      this.inscricaoForm.patchValue({ nomeEquipe: '' });

      // Limpar array de atletas da equipe
      while (this.atletasEquipe.length > 0) {
        this.atletasEquipe.removeAt(0);
      }
    } else {
      // Modo equipe: validar nome da equipe e pelo menos 2 atletas
      this.inscricaoForm.get('nomeEquipe')?.setValidators([Validators.required]);
      this.inscricaoForm.get('atletaNome')?.clearValidators();
      this.inscricaoForm.patchValue({ atletaNome: '' });

      // Adicionar 2 atletas iniciais (será ajustado quando selecionar categoria)
      if (this.atletasEquipe.length === 0) {
        this.adicionarAtletaEquipe();
        this.adicionarAtletaEquipe();
      }
    }

    this.inscricaoForm.get('atletaNome')?.updateValueAndValidity();
    this.inscricaoForm.get('nomeEquipe')?.updateValueAndValidity();
  }

  adicionarAtletaEquipe(): void {
    const atletaGroup = this.fb.group({
      nome: ['', Validators.required],
      isCapitao: [false]
    });

    this.atletasEquipe.push(atletaGroup);
  }

  removerAtletaEquipe(index: number): void {
    if (this.atletasEquipe.length > 2) {
      this.atletasEquipe.removeAt(index);
    }
  }

  setCapitao(index: number): void {
    // Desmarcar todos os outros como capitão
    this.atletasEquipe.controls.forEach((control, i) => {
      control.patchValue({ isCapitao: i === index });
    });
  }

  // Verificar se há capitão selecionado
  temCapitao(): boolean {
    if (this.tipoInscricao !== 'equipe') return true;
    return this.atletasEquipe.controls.some(control => control.get('isCapitao')?.value === true);
  }

  onSubmit(): void {
    if (this.inscricaoForm.invalid) {
      Object.keys(this.inscricaoForm.controls).forEach(key => {
        this.inscricaoForm.get(key)?.markAsTouched();
      });
      return;
    }

    // Validações extras para equipe
    if (this.tipoInscricao === 'equipe') {
      // Verificar quantidade de atletas de acordo com a categoria
      if (this.categoriaSelecionada && this.categoriaSelecionada.quantidadeDeAtletasPorEquipe) {
        const quantidadeRequerida = this.categoriaSelecionada.quantidadeDeAtletasPorEquipe;
        if (this.atletasEquipe.length !== quantidadeRequerida) {
          alert(`Esta categoria requer exatamente ${quantidadeRequerida} atleta(s). Atualmente há ${this.atletasEquipe.length}.`);
          return;
        }
      } else if (this.atletasEquipe.length < 2) {
        alert('A equipe deve ter no mínimo 2 atletas');
        return;
      }

      if (!this.temCapitao()) {
        alert('Selecione um capitão para a equipe');
        return;
      }
    }

    this.isSubmitting = true;

    // Preparar dados para inscrição simplificada
    const inscricaoData: InscricaoSimplificadaCreateRequest = {
      usuarioEmail: this.inscricaoForm.value.usuarioEmail,
      eventoId: this.data.eventoId,
      categoriaId: this.inscricaoForm.value.categoriaId,
      valor: this.inscricaoForm.value.valor,
      codigoDesconto: this.inscricaoForm.value.codigoDesconto,
      valorDesconto: this.inscricaoForm.value.valorDesconto,
      termosAceitos: this.inscricaoForm.value.termosAceitos,
      observacoes: this.inscricaoForm.value.observacoes
    };

    if (this.tipoInscricao === 'individual') {
      // Inscrição individual: enviar nome do atleta
      inscricaoData.atletaNome = this.inscricaoForm.value.atletaNome;
    } else {
      // Inscrição em equipe: enviar nome da equipe e nomes dos atletas
      inscricaoData.nomeEquipe = this.inscricaoForm.value.nomeEquipe;
      inscricaoData.atletasNomes = this.atletasEquipe.controls.map(c => c.get('nome')?.value);
      inscricaoData.capitaoIndex = this.atletasEquipe.controls.findIndex(c => c.get('isCapitao')?.value);
    }

    // Chamar o endpoint simplificado
    this.inscricaoService.criarSimplificada(inscricaoData).subscribe({
      next: (inscricao) => {
        this.isSubmitting = false;
        this.dialogRef.close({ success: true, data: inscricao });
      },
      error: (error) => {
        console.error('Erro ao criar inscrição:', error);
        this.isSubmitting = false;
        alert('Erro ao criar inscrição: ' + (error.error?.message || error.message));
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close({ success: false });
  }
}
