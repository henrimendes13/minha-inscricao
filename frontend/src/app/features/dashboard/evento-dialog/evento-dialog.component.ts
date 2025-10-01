import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';

import { EventoApiResponse, EventoCreateRequest, EventoUpdateRequest } from '../../../models/evento.model';
import { EventoService } from '../../../core/services/evento.service';

export interface EventoDialogData {
  evento?: EventoApiResponse;
  modo: 'criar' | 'editar';
}

@Component({
  selector: 'app-evento-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './evento-dialog.component.html',
  styleUrl: './evento-dialog.component.scss'
})
export class EventoDialogComponent implements OnInit {
  eventoForm!: FormGroup;
  titulo: string;
  isLoading = false;

  estadosBrasileiros = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  constructor(
    private fb: FormBuilder,
    private eventoService: EventoService,
    public dialogRef: MatDialogRef<EventoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EventoDialogData
  ) {
    this.titulo = data.modo === 'criar' ? 'Criar Novo Evento' : 'Editar Evento';
  }

  ngOnInit(): void {
    this.inicializarFormulario();

    if (this.data.modo === 'editar' && this.data.evento) {
      this.preencherFormulario(this.data.evento);
    }
  }

  inicializarFormulario(): void {
    this.eventoForm = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(200)]],
      descricao: ['', [Validators.maxLength(5000)]],
      dataInicioDoEvento: ['', [Validators.required]],
      dataFimDoEvento: ['', [Validators.required]],
      cidade: ['', [Validators.maxLength(100)]],
      estado: ['', [Validators.maxLength(50)]],
      endereco: ['', [Validators.maxLength(300)]]
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
      endereco: evento.endereco || ''
    });
  }

  parseDataBackend(dataStr: string): Date {
    // Formato esperado do backend: yyyy-MM-dd'T'HH:mm:ss ou similar
    return new Date(dataStr);
  }

  onSubmit(): void {
    if (this.eventoForm.invalid) {
      this.eventoForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.eventoForm.value;

    // Converter datas para formato do backend (dd-MM-yyyy)
    const dataInicio = this.eventoService.converterDataParaBackend(formValue.dataInicioDoEvento);
    const dataFim = this.eventoService.converterDataParaBackend(formValue.dataFimDoEvento);

    if (this.data.modo === 'criar') {
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
          this.dialogRef.close({ success: true, evento });
        },
        error: (error) => {
          console.error('Erro ao criar evento:', error);
          this.isLoading = false;
          alert('Erro ao criar evento. Verifique os dados e tente novamente.');
        }
      });
    } else {
      const eventoAtualizado: EventoUpdateRequest = {
        nome: formValue.nome,
        dataInicioDoEvento: dataInicio,
        dataFimDoEvento: dataFim,
        descricao: formValue.descricao || undefined,
        cidade: formValue.cidade || undefined,
        estado: formValue.estado || undefined,
        endereco: formValue.endereco || undefined
      };

      this.eventoService.atualizarEvento(this.data.evento!.id, eventoAtualizado).subscribe({
        next: (evento) => {
          this.dialogRef.close({ success: true, evento });
        },
        error: (error) => {
          console.error('Erro ao atualizar evento:', error);
          this.isLoading = false;
          alert('Erro ao atualizar evento. Verifique os dados e tente novamente.');
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close({ success: false });
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
}
