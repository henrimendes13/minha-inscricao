# Estrutura do Frontend Angular

## 🏗️ Estrutura Criada

### 📁 Organização de Pastas
```
frontend/src/app/
├── core/                    # Serviços singleton, guards, interceptors
│   ├── auth/               # Autenticação
│   │   └── auth.service.ts
│   ├── guards/             # Guards de rota
│   │   └── auth.guard.ts
│   ├── interceptors/       # HTTP Interceptors
│   │   └── auth.interceptor.ts
│   ├── services/           # Serviços base
│   │   └── base-http.service.ts
│   ├── constants/          # Constantes da API
│   │   └── api.constants.ts
│   └── core.module.ts
├── shared/                  # Componentes reutilizáveis
│   └── shared.module.ts
├── features/               # Módulos de funcionalidades (lazy loaded)
│   ├── auth/
│   ├── eventos/
│   ├── inscricoes/
│   ├── leaderboard/
│   ├── atletas/
│   ├── equipes/
│   └── dashboard/
├── layout/                 # Componentes de layout
│   ├── header/
│   │   └── header.component.ts
│   ├── sidebar/
│   │   └── sidebar.component.ts
│   ├── footer/
│   │   └── footer.component.ts
│   └── main-layout/
│       └── main-layout.component.ts
└── models/                 # Interfaces TypeScript
    ├── evento.model.ts
    ├── usuario.model.ts
    ├── inscricao.model.ts
    ├── atleta.model.ts
    ├── equipe.model.ts
    └── index.ts
```

## 🔧 Tecnologias Configuradas

- **Angular 19** com CLI
- **Angular Material** para UI components
- **PrimeNG 19** para componentes avançados
- **NgRx 19** para gerenciamento de estado
- **TypeScript** para tipagem
- **SCSS** para estilização

## 🌐 Funcionalidades Implementadas

### ✅ Core Features
- [x] Autenticação JWT com refresh token
- [x] Interceptor para autorização automática
- [x] Guards de proteção de rotas
- [x] Service base para HTTP requests
- [x] Constantes de API organizadas

### ✅ Layout & Navigation  
- [x] Layout responsivo com sidebar
- [x] Header com menu do usuário
- [x] Navegação baseada em roles
- [x] Footer informativo

### ✅ Roteamento
- [x] Lazy loading para todas as features
- [x] Proteção de rotas com AuthGuard
- [x] Roteamento configurado por módulos

### ✅ Modelos de Dados
- [x] Interfaces TypeScript para todas as entidades
- [x] Enums para status e tipos
- [x] DTOs de request/response organizados

## 🚀 Como Executar

```bash
# Navegar para o frontend
cd frontend

# Instalar dependências (já feito)
npm install

# Executar em desenvolvimento
npm start

# Build para produção
npm run build
```

## 🔗 Proxy Configurado

O proxy está configurado para redirecionar `/api/*` para `http://localhost:8080`, permitindo que o frontend se comunique com a API Spring Boot.

## 📝 Próximos Passos

1. **Implementar componentes de login/register**
2. **Criar services específicos para cada entidade**
3. **Implementar formulários com validação**
4. **Adicionar componentes de listagem/tabelas**
5. **Configurar NgRx stores para gerenciamento de estado**
6. **Implementar testes unitários**
7. **Adicionar internacionalização (i18n)**

## 🎨 Padrões Seguidos

- **Standalone Components** para melhor tree-shaking
- **Lazy Loading** para performance
- **Reactive Forms** para formulários
- **Material Design** para consistência visual
- **Role-based Navigation** para segurança
- **Barrel Exports** para imports organizados