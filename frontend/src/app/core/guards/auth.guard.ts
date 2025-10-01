import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthHelpers } from '../auth/auth-helpers';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    if (this.authService.isAuthenticated()) {
      // Check if route requires specific email (admin check)
      const requiredEmail = route.data?.['requiredEmail'];
      if (requiredEmail) {
        const currentUser = this.authService.getCurrentUser();
        if (currentUser?.email !== requiredEmail) {
          console.warn(`[AUTH-GUARD] Usuário não tem permissão (email requerido: '${requiredEmail}') para acessar ${state.url}`);
          // Redireciona para home se não for o email correto
          this.router.navigate(['/eventos']);
          return false;
        }
      }

      // Check if route requires specific role (legacy support)
      const requiredRole = route.data?.['requiredRole'];
      if (requiredRole && !this.authService.hasRole(requiredRole)) {
        console.warn(`[AUTH-GUARD] Usuário não tem permissão '${requiredRole}' necessária para acessar ${state.url}`);
        // Redireciona para home se não tiver a role necessária
        this.router.navigate(['/eventos']);
        return false;
      }
      return true;
    }

    // Not logged in so redirect to login page
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
  
  /**
   * Método utilitário para verificar se uma rota específica requer autenticação
   * Útil para componentes que querem verificar acesso programaticamente
   */
  public static canAccessRoute(authService: AuthService, requiredRole?: string): boolean {
    if (!authService.isAuthenticated()) {
      return false;
    }
    
    if (requiredRole) {
      return AuthHelpers.userHasRole(authService, requiredRole);
    }
    
    return true;
  }
}
