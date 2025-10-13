import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';

import { Workout, WorkoutType, LeaderboardSummaryDTO } from '../../../models/workout.model';

export interface EditResultadoDialogData {
  result: LeaderboardSummaryDTO;
  workout: Workout;
}

@Component({
  selector: 'app-edit-resultado-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>edit</mat-icon>
      Editar Resultado
    </h2>

    <mat-dialog-content>
      <form [formGroup]="editForm" class="edit-form">
        <div class="participant-info">
          <p><strong>Participante:</strong> {{ data.result.nomeParticipante }}</p>
          <p><strong>Workout:</strong> {{ data.workout.nome }}</p>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Resultado ({{ data.workout.unidadeMedida }})</mat-label>
          <input matInput formControlName="resultadoValor" [placeholder]="getPlaceholder()">
          <mat-hint>{{ getHint() }}</mat-hint>
          <mat-error *ngIf="editForm.get('resultadoValor')?.hasError('required')">
            Resultado é obrigatório
          </mat-error>
        </mat-form-field>

        <div class="checkbox-field">
          <mat-checkbox formControlName="finalizado">
            Marcar como finalizado
          </mat-checkbox>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        Cancelar
      </button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSave()"
        [disabled]="editForm.invalid">
        <mat-icon>save</mat-icon>
        Salvar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .edit-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      min-width: 400px;
      padding: 1rem 0;
    }

    .participant-info {
      background: #f5f5f5;
      padding: 1rem;
      border-radius: 4px;
      margin-bottom: 1rem;
    }

    .participant-info p {
      margin: 0.5rem 0;
    }

    .full-width {
      width: 100%;
    }

    .checkbox-field {
      margin-top: 0.5rem;
    }

    h2 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    mat-dialog-actions {
      margin-top: 1rem;
    }
  `]
})
export class EditResultadoDialogComponent implements OnInit {
  editForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<EditResultadoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EditResultadoDialogData
  ) {}

  ngOnInit(): void {
    console.log('📝 [DIALOG] Iniciando EditResultadoDialog:', {
      nomeParticipante: this.data.result.nomeParticipante,
      workoutTipo: this.data.workout.tipo,
      resultadoAtual: this.data.result.resultadoFormatado,
      finalizado: this.data.result.finalizado
    });

    this.createForm();
  }

  createForm(): void {
    // Parse the current result value to display in the form
    let currentValue = '';

    console.log('🔍 [DIALOG] Fazendo parsing do resultadoFormatado:', this.data.result.resultadoFormatado);

    if (this.data.workout.tipo === WorkoutType.REPS) {
      // For reps, resultadoFormatado is like "120 reps"
      currentValue = this.data.result.resultadoFormatado.replace(/[^\d]/g, '');
      console.log('🔢 [DIALOG] Tipo REPS - Valor extraído:', currentValue);
    } else if (this.data.workout.tipo === WorkoutType.PESO) {
      // For weight, resultadoFormatado is like "80.5 kg"
      currentValue = this.data.result.resultadoFormatado.replace(/[^\d.]/g, '');
      console.log('⚖️ [DIALOG] Tipo PESO - Valor extraído:', currentValue);
    } else {
      // For time, use as-is (format: "12:34" or "1:23:45")
      currentValue = this.data.result.resultadoFormatado.trim();
      console.log('⏱️ [DIALOG] Tipo TEMPO - Valor mantido:', currentValue);
    }

    this.editForm = this.fb.group({
      resultadoValor: [currentValue, Validators.required],
      finalizado: [this.data.result.finalizado]
    });

    console.log('✅ [DIALOG] Form criado com valores:', this.editForm.value);
  }

  getPlaceholder(): string {
    switch (this.data.workout.tipo) {
      case WorkoutType.REPS:
        return 'Ex: 120';
      case WorkoutType.TEMPO:
        return 'Ex: 12:34 ou 1:23:45';
      case WorkoutType.PESO:
        return 'Ex: 80.5';
      default:
        return '';
    }
  }

  getHint(): string {
    switch (this.data.workout.tipo) {
      case WorkoutType.REPS:
        return 'Digite o número de repetições';
      case WorkoutType.TEMPO:
        return 'Digite no formato MM:SS ou HH:MM:SS';
      case WorkoutType.PESO:
        return 'Digite o peso em kg (use ponto para decimais)';
      default:
        return '';
    }
  }

  onSave(): void {
    if (this.editForm.invalid) {
      console.warn('⚠️ [DIALOG] Form inválido. Não pode salvar.');
      return;
    }

    const formValue = this.editForm.value;

    console.log('💾 [DIALOG] Salvando alterações:', {
      valorAnterior: this.data.result.resultadoFormatado,
      novoValor: formValue.resultadoValor,
      finalizadoAnterior: this.data.result.finalizado,
      novoFinalizado: formValue.finalizado
    });

    const dataToReturn = {
      resultadoValor: formValue.resultadoValor.trim(),
      finalizado: formValue.finalizado
    };

    console.log('📤 [DIALOG] Dados que serão retornados:', dataToReturn);

    this.dialogRef.close(dataToReturn);
  }

  onCancel(): void {
    console.log('❌ [DIALOG] Edição cancelada pelo usuário');
    this.dialogRef.close(null);
  }
}
