/**
 * Helpers de autenticação para componentes
 * 
 * Este arquivo contém funções práticas para usar em componentes Angular
 * que facilitam a verificação de autenticação e gestão de estados.
 * 
 * Exemplo de uso em um componente:
 * ```typescript
 * import { AuthHelpers } from '@core/auth/auth-helpers';
 * 
 * export class MeuComponent {
 *   constructor(private authService: AuthService) {}
 * 
 *   ngOnInit() {
 *     // Verificar se usuário está logado
 *     if (AuthHelpers.isUserLoggedIn(this.authService)) {
 *       console.log('Usuário logado!');
 *     }
 * 
 *     // Obter nome do usuário atual
 *     const userName = AuthHelpers.getCurrentUserName(this.authService);
 *   }
 * }
 * ```
 */

import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { decodeJwtPayload, isTokenExpired, hasRole, getUserInfoFromToken, debugToken } from './auth-utils';

export class AuthHelpers {

  /**
   * Verifica se o usuário está logado (método estático para uso fácil)
   * 
   * @param authService Instância do AuthService
   * @returns true se logado, false caso contrário
   * 
   * @example
   * ```typescript
   * // No componente
   * if (AuthHelpers.isUserLoggedIn(this.authService)) {
   *   this.showUserMenu = true;
   * }
   * ```
   */
  static isUserLoggedIn(authService: AuthService): boolean {
    return authService.isAuthenticated();
  }

  /**
   * Obtém o nome do usuário atual
   * 
   * @param authService Instância do AuthService
   * @returns Nome do usuário ou 'Usuário' como padrão
   * 
   * @example
   * ```typescript
   * const userName = AuthHelpers.getCurrentUserName(this.authService);
   * this.welcomeMessage = `Bem-vindo, ${userName}!`;
   * ```
   */
  static getCurrentUserName(authService: AuthService): string {
    const user = authService.getCurrentUser();
    return user?.nome || user?.name || 'Usuário';
  }

  /**
   * Obtém o email do usuário atual
   * 
   * @param authService Instância do AuthService
   * @returns Email do usuário ou null
   * 
   * @example
   * ```typescript
   * const userEmail = AuthHelpers.getCurrentUserEmail(this.authService);
   * if (userEmail) {
   *   this.profileEmail = userEmail;
   * }
   * ```
   */
  static getCurrentUserEmail(authService: AuthService): string | null {
    const user = authService.getCurrentUser();
    return user?.email || null;
  }

  /**
   * Verifica se o usuário tem uma role específica
   * 
   * @param authService Instância do AuthService
   * @param role Role a verificar
   * @returns true se tem a role, false caso contrário
   * 
   * @example
   * ```typescript
   * // Mostrar botão apenas para admins
   * this.showAdminButton = AuthHelpers.userHasRole(this.authService, 'ADMIN');
   * 
   * // Verificar se pode criar eventos
   * if (AuthHelpers.userHasRole(this.authService, 'ORGANIZADOR')) {
   *   this.canCreateEvents = true;
   * }
   * ```
   */
  static userHasRole(authService: AuthService, role: string): boolean {
    const token = authService.getToken();
    return hasRole(token, role);
  }

  /**
   * Verifica se o usuário é administrador
   * 
   * @param authService Instância do AuthService
   * @returns true se é admin, false caso contrário
   * 
   * @example
   * ```typescript
   * if (AuthHelpers.isAdmin(this.authService)) {
   *   this.router.navigate(['/admin']);
   * }
   * ```
   */
  static isAdmin(authService: AuthService): boolean {
    return this.userHasRole(authService, 'ADMIN');
  }

  /**
   * Verifica se o usuário é organizador
   * 
   * @param authService Instância do AuthService
   * @returns true se é organizador, false caso contrário
   * 
   * @example
   * ```typescript
   * this.canManageEvents = AuthHelpers.isOrganizador(this.authService);
   * ```
   */
  static isOrganizador(authService: AuthService): boolean {
    return this.userHasRole(authService, 'ORGANIZADOR');
  }

  /**
   * Verifica se o usuário é atleta
   * 
   * @param authService Instância do AuthService
   * @returns true se é atleta, false caso contrário
   * 
   * @example
   * ```typescript
   * this.canRegisterForEvents = AuthHelpers.isAtleta(this.authService);
   * ```
   */
  static isAtleta(authService: AuthService): boolean {
    return this.userHasRole(authService, 'ATLETA');
  }

  /**
   * Obtém o tipo de usuário como string amigável
   * 
   * @param authService Instância do AuthService
   * @returns Tipo do usuário formatado
   * 
   * @example
   * ```typescript
   * const userType = AuthHelpers.getUserTypeDisplay(this.authService);
   * // Retorna: "Administrador", "Organizador", "Atleta" ou "Usuário"
   * ```
   */
  static getUserTypeDisplay(authService: AuthService): string {
    if (this.isAdmin(authService)) return 'Administrador';
    if (this.isOrganizador(authService)) return 'Organizador';
    if (this.isAtleta(authService)) return 'Atleta';
    return 'Usuário';
  }

  /**
   * Verifica se o token está próximo da expiração
   * 
   * @param authService Instância do AuthService
   * @returns true se expira em menos de 5 minutos
   * 
   * @example
   * ```typescript
   * if (AuthHelpers.isTokenNearExpiration(this.authService)) {
   *   this.showRenewTokenWarning = true;
   * }
   * ```
   */
  static isTokenNearExpiration(authService: AuthService): boolean {
    const token = authService.getToken();
    const payload = decodeJwtPayload(token);
    
    if (!payload || !payload.exp) return false;
    
    const expirationTime = payload.exp * 1000;
    const currentTime = Date.now();
    const timeLeft = expirationTime - currentTime;
    const minutesLeft = Math.floor(timeLeft / (1000 * 60));
    
    return minutesLeft > 0 && minutesLeft < 5;
  }

  /**
   * Observable que emite quando o estado de autenticação muda
   * 
   * @param authService Instância do AuthService
   * @returns Observable<boolean> que emite true quando logado, false quando deslogado
   * 
   * @example
   * ```typescript
   * AuthHelpers.getAuthState$(this.authService).subscribe(isLoggedIn => {
   *   this.isLoggedIn = isLoggedIn;
   *   if (isLoggedIn) {
   *     this.loadUserData();
   *   }
   * });
   * ```
   */
  static getAuthState$(authService: AuthService): Observable<boolean> {
    return authService.currentUser$.pipe(
      map(user => !!user),
      catchError(error => {
        console.error('[AUTH-HELPERS] Erro ao obter estado de autenticação:', error);
        return of(false);
      })
    );
  }

  /**
   * Observable que emite os dados do usuário atual
   * 
   * @param authService Instância do AuthService
   * @returns Observable com dados do usuário ou null
   * 
   * @example
   * ```typescript
   * AuthHelpers.getCurrentUser$(this.authService).subscribe(user => {
   *   if (user) {
   *     this.userName = user.nome;
   *     this.userEmail = user.email;
   *   }
   * });
   * ```
   */
  static getCurrentUser$(authService: AuthService): Observable<any> {
    return authService.currentUser$.pipe(
      catchError(error => {
        console.error('[AUTH-HELPERS] Erro ao obter usuário atual:', error);
        return of(null);
      })
    );
  }

  /**
   * Função para fazer logout com confirmação opcional
   * 
   * @param authService Instância do AuthService
   * @param confirmLogout Se deve mostrar confirmação antes de fazer logout
   * @returns Promise<boolean> - true se fez logout, false se cancelou
   * 
   * @example
   * ```typescript
   * // Logout direto
   * AuthHelpers.logout(this.authService);
   * 
   * // Logout com confirmação
   * const loggedOut = await AuthHelpers.logout(this.authService, true);
   * if (loggedOut) {
   *   this.showLogoutSuccessMessage();
   * }
   * ```
   */
  static async logout(authService: AuthService, confirmLogout: boolean = false): Promise<boolean> {
    if (confirmLogout) {
      const confirmed = window.confirm('Tem certeza que deseja sair?');
      if (!confirmed) {
        return false;
      }
    }
    
    try {
      authService.logout();
      return true;
    } catch (error) {
      console.error('[AUTH-HELPERS] Erro durante logout:', error);
      return false;
    }
  }

  /**
   * Verifica se o usuário pode acessar uma rota específica
   * 
   * @param authService Instância do AuthService
   * @param requiredRole Role necessária para acesso (opcional)
   * @returns true se pode acessar, false caso contrário
   * 
   * @example
   * ```typescript
   * // Verificar se pode acessar área admin
   * if (AuthHelpers.canAccessRoute(this.authService, 'ADMIN')) {
   *   this.showAdminMenu = true;
   * }
   * 
   * // Verificar se apenas precisa estar logado
   * if (AuthHelpers.canAccessRoute(this.authService)) {
   *   this.showProtectedContent = true;
   * }
   * ```
   */
  static canAccessRoute(authService: AuthService, requiredRole?: string): boolean {
    if (!this.isUserLoggedIn(authService)) {
      return false;
    }
    
    if (requiredRole) {
      return this.userHasRole(authService, requiredRole);
    }
    
    return true;
  }

  /**
   * Obtém informações de debug sobre a autenticação atual
   * Útil durante desenvolvimento para entender problemas
   * 
   * @param authService Instância do AuthService
   * @returns Objeto com informações de debug
   * 
   * @example
   * ```typescript
   * // Durante desenvolvimento
   * console.log('Auth Debug:', AuthHelpers.getAuthDebugInfo(this.authService));
   * ```
   */
  static getAuthDebugInfo(authService: AuthService): any {
    const token = authService.getToken();
    const currentUser = authService.getCurrentUser();
    const isAuthenticated = authService.isAuthenticated();
    const tokenDebug = debugToken(token);
    
    return {
      isAuthenticated,
      currentUser,
      token: token ? '***' + token.slice(-10) : null, // Mostra apenas o final do token por segurança
      tokenDebug,
      userRoles: {
        isAdmin: this.isAdmin(authService),
        isOrganizador: this.isOrganizador(authService),
        isAtleta: this.isAtleta(authService)
      }
    };
  }

  /**
   * Formata o tempo restante até a expiração do token
   * 
   * @param authService Instância do AuthService
   * @returns String formatada com tempo restante
   * 
   * @example
   * ```typescript
   * const timeLeft = AuthHelpers.getTokenExpirationFormatted(this.authService);
   * // Retorna: "Expira em 25 minutos" ou "Token expirado"
   * ```
   */
  static getTokenExpirationFormatted(authService: AuthService): string {
    const token = authService.getToken();
    const payload = decodeJwtPayload(token);
    
    if (!payload || !payload.exp) {
      return 'Token inválido';
    }
    
    const expirationTime = payload.exp * 1000;
    const currentTime = Date.now();
    const timeLeft = expirationTime - currentTime;
    
    if (timeLeft <= 0) {
      return 'Token expirado';
    }
    
    const minutesLeft = Math.floor(timeLeft / (1000 * 60));
    const hoursLeft = Math.floor(minutesLeft / 60);
    
    if (hoursLeft > 0) {
      const remainingMinutes = minutesLeft % 60;
      return `Expira em ${hoursLeft}h ${remainingMinutes}min`;
    } else {
      return `Expira em ${minutesLeft} minutos`;
    }
  }
}