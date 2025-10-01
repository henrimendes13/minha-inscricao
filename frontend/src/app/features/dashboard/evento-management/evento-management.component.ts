import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';

import { EventoService } from '../../../core/services/evento.service';
import { EventoApiResponse } from '../../../models/evento.model';
import { EventoDialogComponent, EventoDialogData } from '../evento-dialog/evento-dialog.component';

@Component({
  selector: 'app-evento-management',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatChipsModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatCardModule
  ],
  templateUrl: './evento-management.component.html',
  styleUrl: './evento-management.component.scss'
})
export class EventoManagementComponent implements OnInit {
  eventos: EventoApiResponse[] = [];
  displayedColumns: string[] = ['nome', 'datas', 'localizacao', 'status', 'inscricoes', 'acoes'];
  isLoading = true;
  error: string | null = null;

  constructor(
    private eventoService: EventoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregarEventos();
  }

  carregarEventos(): void {
    this.isLoading = true;
    this.error = null;

    this.eventoService.listarEventos().subscribe({
      next: (eventos) => {
        this.eventos = eventos;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar eventos:', error);
        this.error = 'Erro ao carregar eventos';
        this.isLoading = false;
        this.showSnackBar('Erro ao carregar eventos', 'error');
      }
    });
  }

  abrirDialogCriar(): void {
    const dialogRef = this.dialog.open(EventoDialogComponent, {
      width: '600px',
      data: { modo: 'criar' } as EventoDialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.success) {
        this.showSnackBar('Evento criado com sucesso!', 'success');
        this.carregarEventos();
      }
    });
  }

  abrirDialogEditar(evento: EventoApiResponse): void {
    const dialogRef = this.dialog.open(EventoDialogComponent, {
      width: '600px',
      data: { modo: 'editar', evento } as EventoDialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.success) {
        this.showSnackBar('Evento atualizado com sucesso!', 'success');
        this.carregarEventos();
      }
    });
  }

  deletarEvento(evento: EventoApiResponse): void {
    if (!confirm(`Tem certeza que deseja deletar o evento "${evento.nome}"?`)) {
      return;
    }

    this.eventoService.deletarEvento(evento.id).subscribe({
      next: () => {
        this.showSnackBar('Evento deletado com sucesso!', 'success');
        this.carregarEventos();
      },
      error: (error) => {
        console.error('Erro ao deletar evento:', error);
        this.showSnackBar('Erro ao deletar evento', 'error');
      }
    });
  }

  mudarStatus(evento: EventoApiResponse, novoStatus: string): void {
    const acoes: { [key: string]: () => any } = {
      'ABERTO': () => this.eventoService.publicarEvento(evento.id),
      'INSCRICOES_ENCERRADAS': () => this.eventoService.encerrarInscricoes(evento.id),
      'EM_ANDAMENTO': () => this.eventoService.iniciarEvento(evento.id),
      'FINALIZADO': () => this.eventoService.finalizarEvento(evento.id),
      'CANCELADO': () => this.eventoService.cancelarEvento(evento.id),
      'ADIADO': () => this.eventoService.adiarEvento(evento.id)
    };

    const acao = acoes[novoStatus];
    if (!acao) {
      this.showSnackBar('Status inválido', 'error');
      return;
    }

    acao().subscribe({
      next: () => {
        this.showSnackBar(`Status alterado para ${novoStatus}!`, 'success');
        this.carregarEventos();
      },
      error: (error: any) => {
        console.error('Erro ao mudar status:', error);
        this.showSnackBar('Erro ao mudar status do evento', 'error');
      }
    });
  }

  getStatusColor(status: string): string {
    return this.eventoService.getStatusColor(status);
  }

  formatarData(dataStr: string): string {
    const data = new Date(dataStr);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatarDataRange(dataInicio: string, dataFim: string): string {
    const inicio = this.formatarData(dataInicio);
    const fim = this.formatarData(dataFim);

    if (inicio === fim) {
      return inicio;
    }
    return `${inicio} - ${fim}`;
  }

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  private showSnackBar(message: string, type: 'success' | 'error'): void {
    this.snackBar.open(message, 'Fechar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: type === 'success' ? ['success-snackbar'] : ['error-snackbar']
    });
  }
}
