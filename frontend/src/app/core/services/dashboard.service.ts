import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from './base-http.service';
import { DashboardEstatisticas } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class DashboardService extends BaseHttpService {

  /**
   * Obtém estatísticas consolidadas do dashboard
   * Requer permissão ADMIN
   */
  obterEstatisticas(): Observable<DashboardEstatisticas> {
    return this.get<DashboardEstatisticas>('/dashboard/estatisticas');
  }
}
