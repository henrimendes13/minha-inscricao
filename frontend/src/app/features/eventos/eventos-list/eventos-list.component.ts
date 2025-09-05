import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-eventos-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="eventos-container">
      <h1>Lista de Eventos Esportivos</h1>
      
      <div class="eventos-grid">
        <mat-card class="evento-card" *ngFor="let evento of eventos">
          <mat-card-header>
            <mat-icon mat-card-avatar>event</mat-icon>
            <mat-card-title>{{ evento.nome }}</mat-card-title>
            <mat-card-subtitle>{{ evento.local }} - {{ evento.data }}</mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <p>{{ evento.descricao }}</p>
            <div class="evento-info">
              <span class="info-item">
                <mat-icon>people</mat-icon>
                {{ evento.inscritos }} inscritos
              </span>
              <span class="info-item">
                <mat-icon>schedule</mat-icon>
                {{ evento.horario }}
              </span>
            </div>
          </mat-card-content>
          
          <mat-card-actions>
            <button mat-raised-button color="primary">Ver Detalhes</button>
            <button mat-button color="accent">Inscrever-se</button>
          </mat-card-actions>
        </mat-card>
      </div>

      <div class="info-section">
        <mat-card>
          <mat-card-content>
            <h3>🎉 Frontend Angular Funcionando!</h3>
            <p>
              Parabéns! A estrutura Angular está rodando corretamente sem necessidade de login.
              Você pode ver esta tela de eventos diretamente.
            </p>
            <ul>
              <li>✅ Roteamento funcionando</li>
              <li>✅ Components carregando</li>
              <li>✅ Angular Material renderizando</li>
              <li>✅ Acesso direto a /eventos configurado</li>
            </ul>
            <p><strong>Próximos passos:</strong> Conectar com a API Spring Boot para buscar eventos reais.</p>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .eventos-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    h1 {
      text-align: center;
      margin-bottom: 30px;
      color: #333;
    }

    .eventos-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 20px;
      margin-bottom: 40px;
    }

    .evento-card {
      transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
    }

    .evento-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }

    .evento-info {
      display: flex;
      gap: 20px;
      margin-top: 10px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 5px;
      font-size: 14px;
      color: #666;
    }

    .info-item mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    mat-card-header mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #1976d2;
    }

    mat-card-actions {
      padding: 16px;
      gap: 8px;
    }

    .info-section {
      margin-top: 40px;
    }

    .info-section ul {
      margin: 16px 0;
    }

    .info-section li {
      margin: 8px 0;
      font-size: 14px;
    }

    @media (max-width: 600px) {
      .eventos-container {
        padding: 16px;
      }
      
      .eventos-grid {
        grid-template-columns: 1fr;
      }
      
      .evento-info {
        flex-direction: column;
        gap: 8px;
      }
    }
  `]
})
export class EventosListComponent {
  eventos = [
    {
      nome: 'Copa de Futebol 2025',
      local: 'Estádio Municipal',
      data: '15/03/2025',
      horario: '14:00',
      descricao: 'Torneio de futebol amador com premiação para os 3 primeiros colocados.',
      inscritos: 24
    },
    {
      nome: 'Maratona da Cidade',
      local: 'Centro da Cidade',
      data: '22/03/2025', 
      horario: '06:00',
      descricao: 'Corrida de 21km pelas principais ruas da cidade.',
      inscritos: 156
    },
    {
      nome: 'Torneio de Vôlei',
      local: 'Ginásio Esportivo',
      data: '05/04/2025',
      horario: '09:00',
      descricao: 'Campeonato de vôlei por equipes mistas.',
      inscritos: 12
    }
  ];
}