import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { AuthService } from '../auth/auth.service';
import { AuthHelpers } from '../auth/auth-helpers';
import { EventoService } from '../services/evento.service';

@Injectable({
  providedIn: 'root'
})
export class EventoOwnerGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private eventoService: EventoService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    // Verificar se está autenticado
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // Se for admin, pode acessar qualquer evento
    if (AuthHelpers.isAdmin(this.authService)) {
      return true;
    }

    // Obter eventoId da rota
    const eventoIdParam = route.paramMap.get('eventoId');
    if (!eventoIdParam) {
      console.error('[EVENTO-OWNER-GUARD] Parâmetro eventoId não encontrado na rota');
      this.router.navigate(['/eventos']);
      return false;
    }

    const eventoId = parseInt(eventoIdParam, 10);
    if (isNaN(eventoId)) {
      console.error('[EVENTO-OWNER-GUARD] Parâmetro eventoId inválido:', eventoIdParam);
      this.router.navigate(['/eventos']);
      return false;
    }

    // Verificar se é o dono do evento
    return this.checkEventoOwnership(eventoId);
  }

  private checkEventoOwnership(eventoId: number): Observable<boolean> {
    const currentUserEmail = AuthHelpers.getCurrentUserEmail(this.authService);
    
    if (!currentUserEmail) {
      console.warn('[EVENTO-OWNER-GUARD] Usuário sem email - negando acesso');
      this.router.navigate(['/eventos']);
      return of(false);
    }

    return this.eventoService.buscarEventoPorId(eventoId).pipe(
      map(evento => {
        // Verificar se o usuário é o organizador do evento
        const isOwner = evento.organizadorEmail === currentUserEmail;
        
        if (!isOwner) {
          console.warn(`[EVENTO-OWNER-GUARD] Usuário ${currentUserEmail} não é dono do evento ${eventoId}`);
          this.router.navigate(['/eventos'], {
            queryParams: { 
              message: 'Acesso negado: você não tem permissão para gerenciar este evento' 
            }
          });
        }
        
        return isOwner;
      }),
      catchError(error => {
        console.error('[EVENTO-OWNER-GUARD] Erro ao verificar ownership do evento:', error);
        this.router.navigate(['/eventos'], {
          queryParams: { 
            message: 'Erro ao verificar permissões do evento' 
          }
        });
        return of(false);
      })
    );
  }
}