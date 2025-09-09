# 🔐 Sistema de Autenticação - Guia Completo

## Visão Geral

Este sistema de autenticação foi projetado para ser robusto, fácil de usar e amigável para desenvolvedores iniciantes. Ele gerencia login, logout, verificação de tokens JWT e proteção de rotas automaticamente.

## 📁 Estrutura dos Arquivos

```
core/auth/
├── auth.service.ts        # Serviço principal de autenticação
├── auth-utils.ts          # Utilitários para trabalhar com JWT
├── auth-helpers.ts        # Helpers para usar em componentes
├── AUTH_README.md         # Esta documentação
├── guards/
│   └── auth.guard.ts      # Guard para proteger rotas
└── interceptors/
    └── auth.interceptor.ts # Interceptor para requisições HTTP
```

## 🚀 Como Verificar se o Usuário Está Logado

### Método 1: No Componente (Recomendado)
```typescript
import { AuthHelpers } from '@core/auth/auth-helpers';

export class MeuComponent {
  isLoggedIn = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Verificação simples
    this.isLoggedIn = AuthHelpers.isUserLoggedIn(this.authService);

    // Reagir a mudanças de autenticação
    AuthHelpers.getAuthState$(this.authService).subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        console.log('Usuário logou!');
      }
    });
  }
}
```

### Método 2: Usando o AuthService Diretamente
```typescript
export class MeuComponent {
  constructor(private authService: AuthService) {}

  verificarLogin() {
    if (this.authService.isAuthenticated()) {
      console.log('Usuário está logado!');
      
      // Obter dados do usuário
      const user = this.authService.getCurrentUser();
      console.log('Nome:', user?.nome);
      console.log('Email:', user?.email);
    }
  }
}
```

### Método 3: No Template HTML
```html
<!-- Mostrar conteúdo apenas para usuários logados -->
<div *ngIf="isLoggedIn">
  <h2>Bem-vindo, {{ userName }}!</h2>
  <button (click)="logout()">Sair</button>
</div>

<!-- Mostrar botão de login para usuários deslogados -->
<div *ngIf="!isLoggedIn">
  <button routerLink="/login">Fazer Login</button>
</div>
```

## 🔑 Trabalhando com Tokens JWT

### Obter e Verificar Token
```typescript
import { debugToken, isTokenExpired, getUserInfoFromToken } from '@core/auth/auth-utils';

export class MeuComponent {
  constructor(private authService: AuthService) {}

  verificarToken() {
    const token = this.authService.getToken();
    
    if (token) {
      console.log('Token encontrado!');
      
      // Verificar se está expirado
      if (isTokenExpired(token)) {
        console.log('Token expirado - usuário precisa fazer login novamente');
      } else {
        console.log('Token válido!');
        
        // Obter informações do usuário do token
        const userInfo = getUserInfoFromToken(token);
        console.log('Dados do token:', userInfo);
      }
      
      // Debug completo (apenas em desenvolvimento)
      console.log('Debug do token:', debugToken(token));
    } else {
      console.log('Nenhum token encontrado - usuário não logado');
    }
  }
}
```

## 👥 Verificando Roles/Permissões

### Verificar Tipo de Usuário
```typescript
import { AuthHelpers } from '@core/auth/auth-helpers';

export class MeuComponent {
  constructor(private authService: AuthService) {}

  verificarPermissoes() {
    // Verificar se é admin
    if (AuthHelpers.isAdmin(this.authService)) {
      console.log('Usuário é administrador');
      this.mostrarMenuAdmin = true;
    }

    // Verificar se é organizador
    if (AuthHelpers.isOrganizador(this.authService)) {
      console.log('Usuário pode criar eventos');
      this.podeGerenciarEventos = true;
    }

    // Verificar se é atleta
    if (AuthHelpers.isAtleta(this.authService)) {
      console.log('Usuário pode se inscrever em eventos');
      this.podeSeInscrever = true;
    }

    // Verificar role específica
    if (AuthHelpers.userHasRole(this.authService, 'CUSTOM_ROLE')) {
      console.log('Usuário tem role customizada');
    }
  }
}
```

### No Template HTML
```html
<!-- Mostrar menu apenas para admins -->
<div *ngIf="AuthHelpers.isAdmin(authService)">
  <button>Painel Admin</button>
</div>

<!-- Mostrar opções para organizadores -->
<div *ngIf="AuthHelpers.isOrganizador(authService)">
  <button>Criar Evento</button>
  <button>Gerenciar Eventos</button>
</div>
```

## 🛡️ Protegendo Rotas

### Configurar Rotas Protegidas
```typescript
// app.routes.ts
import { AuthGuard } from '@core/guards/auth.guard';

export const routes: Routes = [
  // Rota pública
  { path: 'eventos', component: EventosComponent },
  
  // Rota que requer login
  { 
    path: 'perfil', 
    component: PerfilComponent,
    canActivate: [AuthGuard]
  },
  
  // Rota que requer role específica
  { 
    path: 'admin', 
    component: AdminComponent,
    canActivate: [AuthGuard],
    data: { requiredRole: 'ADMIN' }
  }
];
```

### Verificar Acesso Programaticamente
```typescript
import { AuthGuard } from '@core/guards/auth.guard';

export class MeuComponent {
  constructor(private authService: AuthService) {}

  verificarAcesso() {
    // Verificar se pode acessar área admin
    if (AuthGuard.canAccessRoute(this.authService, 'ADMIN')) {
      this.router.navigate(['/admin']);
    } else {
      console.log('Sem permissão para área admin');
    }

    // Verificar se pode acessar qualquer área protegida
    if (AuthGuard.canAccessRoute(this.authService)) {
      this.mostrarConteudoProtegido = true;
    }
  }
}
```

## 📡 Estados de Loading

### Usar Loading States
```typescript
export class LoginComponent {
  isLoading = false;
  isLoggingIn = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Observar estado geral de loading
    this.authService.isLoading$.subscribe(loading => {
      this.isLoading = loading;
    });

    // Observar estado específico de login
    this.authService.isLoggingIn$.subscribe(loggingIn => {
      this.isLoggingIn = loggingIn;
    });
  }
}
```

### No Template
```html
<!-- Mostrar spinner durante login -->
<button [disabled]="isLoggingIn" (click)="login()">
  <mat-spinner *ngIf="isLoggingIn" diameter="20"></mat-spinner>
  <span *ngIf="!isLoggingIn">Entrar</span>
  <span *ngIf="isLoggingIn">Entrando...</span>
</button>
```

## 🎯 Exemplos Práticos Completos

### 1. Componente de Header com Autenticação
```typescript
import { Component } from '@angular/core';
import { AuthHelpers } from '@core/auth/auth-helpers';

@Component({
  selector: 'app-header',
  template: `
    <header>
      <!-- Menu para usuários logados -->
      <div *ngIf="isLoggedIn">
        <span>Olá, {{ userName }}!</span>
        <button (click)="logout()">Sair</button>
      </div>
      
      <!-- Botão de login para usuários deslogados -->
      <div *ngIf="!isLoggedIn">
        <button routerLink="/login">Entrar</button>
      </div>
    </header>
  `
})
export class HeaderComponent implements OnInit {
  isLoggedIn = false;
  userName = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Reagir a mudanças de autenticação
    AuthHelpers.getAuthState$(this.authService).subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        this.userName = AuthHelpers.getCurrentUserName(this.authService);
      }
    });
  }

  async logout() {
    const success = await AuthHelpers.logout(this.authService, true);
    if (success) {
      console.log('Logout realizado com sucesso');
    }
  }
}
```

### 2. Guard Personalizado
```typescript
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthHelpers } from '@core/auth/auth-helpers';

@Injectable()
export class AdminOnlyGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (AuthHelpers.isAdmin(this.authService)) {
      return true;
    }
    
    this.router.navigate(['/eventos']);
    return false;
  }
}
```

## 🐛 Debug e Troubleshooting

### Verificar Estado da Autenticação
```typescript
// Cole isto no console do browser ou use em desenvolvimento
export class DebugComponent {
  constructor(private authService: AuthService) {}

  debugAuth() {
    const debugInfo = AuthHelpers.getAuthDebugInfo(this.authService);
    console.log('🐛 Debug de Autenticação:', debugInfo);
  }
}
```

### Logs Úteis
O sistema gera logs automáticos. Procure por:
- `[AUTH-SERVICE]` - Operações do serviço de autenticação
- `[AUTH-GUARD]` - Verificações de acesso a rotas
- `[AUTH-INTERCEPTOR]` - Problemas com requisições HTTP
- `[AUTH-UTILS]` - Operações com tokens JWT

## ❓ FAQ - Perguntas Frequentes

### P: Como saber se o usuário está logado?
**R:** Use `AuthHelpers.isUserLoggedIn(this.authService)` ou `this.authService.isAuthenticated()`

### P: Como obter o token do usuário?
**R:** Use `this.authService.getToken()`

### P: Como verificar se o usuário é admin?
**R:** Use `AuthHelpers.isAdmin(this.authService)`

### P: Como reagir quando o usuário faz login/logout?
**R:** Assine o Observable: `AuthHelpers.getAuthState$(this.authService).subscribe(isLoggedIn => {...})`

### P: Como proteger uma rota?
**R:** Adicione `canActivate: [AuthGuard]` na configuração da rota

### P: O token expira automaticamente?
**R:** Sim, o sistema verifica e limpa tokens expirados automaticamente

### P: Como fazer logout programático?
**R:** Use `AuthHelpers.logout(this.authService)` ou `this.authService.logout()`

## 🔧 Configurações Avançadas

### Personalizar Mensagens de Erro
As mensagens são configuradas no `AuthInterceptor`. Para personalizar, edite os métodos `handle403Forbidden`, `handleNetworkError`, etc.

### Adicionar Novos Tipos de Usuário
1. Adicione a role no backend
2. Crie método helper em `AuthHelpers` (ex: `isCustomRole`)
3. Use nas verificações: `AuthHelpers.isCustomRole(authService)`

### Logs Personalizados
Para desabilitar logs em produção, envolva os `console.log` em verificações de ambiente:
```typescript
if (!environment.production) {
  console.log('[AUTH-SERVICE] Log apenas em dev');
}
```

---

## 🎉 Conclusão

Este sistema foi projetado para ser **fácil de usar** mesmo para desenvolvedores iniciantes. As principais vantagens:

✅ **Simples**: Use `AuthHelpers.isUserLoggedIn()` para verificar login
✅ **Automático**: Tokens são gerenciados automaticamente
✅ **Seguro**: Rotas protegidas por guards
✅ **Feedback**: Estados de loading e mensagens de erro
✅ **Debug**: Ferramentas para entender problemas
✅ **Flexível**: Suporte a diferentes roles/permissões

**Precisa de ajuda?** Procure pelos logs com `[AUTH-` no console do navegador ou use `AuthHelpers.getAuthDebugInfo()` para debug completo.