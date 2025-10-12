import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { InscricaoService } from '../../../core/services/inscricao.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { AtletaService } from '../../../core/services/atleta.service';
import { EquipeService } from '../../../core/services/equipe.service';

import { InscricaoCreateRequest } from '../../../models/inscricao.model';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './inscricao-form-dialog.component.html',
  styleUrls: ['./inscricao-form-dialog.component.scss']
})
export class InscricaoFormDialogComponent implements OnInit {
  inscricaoForm!: FormGroup;
  isSubmitting = false;
  isLoadingData = true;

  // Dados para dropdowns
  usuarios: any[] = [];
  categorias: any[] = [];
  atletas: any[] = [];
  equipes: any[] = [];

  // Tipo de inscrição
  tipoInscricao: 'individual' | 'equipe' = 'individual';

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<InscricaoFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private inscricaoService: InscricaoService,
    private usuarioService: UsuarioService,
    private categoriaService: CategoriaService,
    private atletaService: AtletaService,
    private equipeService: EquipeService
  ) {}

  ngOnInit(): void {
    this.createForm();
    this.loadDropdownData();
  }

  createForm(): void {
    this.inscricaoForm = this.fb.group({
      usuarioInscricaoId: [null, Validators.required],
      categoriaId: [null, Validators.required],
      atletaId: [null],
      equipeId: [null],
      valor: [0, [Validators.required, Validators.min(0)]],
      codigoDesconto: [''],
      valorDesconto: [0, Validators.min(0)],
      termosAceitos: [false, Validators.requiredTrue],
      observacoes: ['']
    });

    // Atualizar validações quando o tipo muda
    this.inscricaoForm.get('atletaId')?.setValidators([Validators.required]);
    this.inscricaoForm.get('atletaId')?.updateValueAndValidity();
  }

  loadDropdownData(): void {
    this.isLoadingData = true;

    Promise.all([
      this.usuarioService.buscarTodos().toPromise(),
      this.categoriaService.buscarPorEvento(this.data.eventoId).toPromise(),
      this.atletaService.buscarTodos().toPromise(),
      this.equipeService.buscarPorEvento(this.data.eventoId).toPromise()
    ]).then(([usuarios, categorias, atletas, equipes]) => {
      this.usuarios = usuarios || [];
      this.categorias = categorias || [];
      this.atletas = atletas || [];
      this.equipes = equipes || [];
      this.isLoadingData = false;
    }).catch(error => {
      console.error('Erro ao carregar dados:', error);
      this.isLoadingData = false;
    });
  }

  onTipoInscricaoChange(tipo: 'individual' | 'equipe'): void {
    this.tipoInscricao = tipo;

    if (tipo === 'individual') {
      this.inscricaoForm.get('atletaId')?.setValidators([Validators.required]);
      this.inscricaoForm.get('equipeId')?.clearValidators();
      this.inscricaoForm.patchValue({ equipeId: null });
    } else {
      this.inscricaoForm.get('equipeId')?.setValidators([Validators.required]);
      this.inscricaoForm.get('atletaId')?.clearValidators();
      this.inscricaoForm.patchValue({ atletaId: null });
    }

    this.inscricaoForm.get('atletaId')?.updateValueAndValidity();
    this.inscricaoForm.get('equipeId')?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.inscricaoForm.invalid) {
      Object.keys(this.inscricaoForm.controls).forEach(key => {
        this.inscricaoForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;

    const inscricaoData: InscricaoCreateRequest = {
      ...this.inscricaoForm.value,
      eventoId: this.data.eventoId
    };

    this.inscricaoService.criar(inscricaoData).subscribe({
      next: (inscricao) => {
        this.isSubmitting = false;
        this.dialogRef.close({ success: true, data: inscricao });
      },
      error: (error) => {
        console.error('Erro ao criar inscrição:', error);
        this.isSubmitting = false;
        this.dialogRef.close({ success: false, error: error });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close({ success: false });
  }
}
