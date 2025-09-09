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
      // Check if route requires specific role
      const requiredRole = route.data?.['requiredRole'];
      if (requiredRole && !this.authService.hasRole(requiredRole)) {
        this.router.navigate(['/dashboard']);
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
