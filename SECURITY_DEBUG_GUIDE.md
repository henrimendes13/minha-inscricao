# Spring Security 403 Error Debug Guide

## Resumo do Problema

A aplicação está retornando **403 Forbidden** para `POST /api/usuarios`, mesmo estando configurado como `permitAll()` na configuração de segurança.

## Mudanças Implementadas para Debug

### 1. Logging Detalhado (application.properties)

```properties
# Spring Security Debug Logging - COMPREHENSIVE
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.web=DEBUG
logging.level.org.springframework.security.web.access=DEBUG
logging.level.org.springframework.security.web.authentication=DEBUG
logging.level.org.springframework.security.web.FilterChainProxy=DEBUG
logging.level.org.springframework.security.access=DEBUG
logging.level.org.springframework.security.access.intercept=DEBUG
logging.level.org.springframework.security.web.access.intercept=DEBUG
logging.level.org.springframework.web.cors=DEBUG
logging.level.org.springframework.security.config=DEBUG

# Enable Spring Security Debug Mode (EXTREMELY VERBOSE)
spring.security.debug=true
```

### 2. SecurityConfig.java Melhorado

- **@Order(1) e @Order(2)**: Criamos duas filter chains separadas
  - **Chain 1 (Order 1)**: Para endpoints completamente públicos
  - **Chain 2 (Order 2)**: Para endpoints protegidos

- **Logging Detalhado**: Logs extensivos durante a configuração
- **Matchers Explícitos**: RequestMatchers explícitos para debug
- **CORS Configurado**: Configuração CORS mais robusta
- **Headers de Segurança**: Configuração adequada para desenvolvimento

### 3. SecurityDebugFilter.java

Filtro customizado que registra:
- Detalhes completos de cada request
- Estado do SecurityContext antes e depois
- Headers HTTP
- Informações de autenticação
- Análise específica para erros 403

### 4. DebugController.java

Endpoints de teste para validar configuração:
- `GET /api/debug/public`
- `POST /api/debug/public`
- `GET /api/debug/security-context`

### 5. SecurityConfigMinimal.java

Configuração minimal para testes extremos:
- Desabilita TUDO relacionado à segurança
- Permite todos os requests
- Ativada com: `security.config.minimal=true`

## Como Debugar

### Passo 1: Executar com Logs Detalhados

1. Inicie a aplicação com `mvn spring-boot:run`
2. Monitore os logs para ver:
   ```
   🔧 CONFIGURANDO SPRING SECURITY FILTER CHAIN
   🔓 ENDPOINTS PÚBLICOS CONFIGURADOS:
      - POST /api/usuarios
      - POST /api/usuarios/com-organizador
      ...
   ```

### Passo 2: Testar Endpoints de Debug

Execute o script PowerShell:
```powershell
.\test-security.ps1
```

Ou manualmente:
```bash
# Testar debug endpoints
curl -X GET http://localhost:8080/api/debug/public
curl -X POST http://localhost:8080/api/debug/public -H "Content-Type: application/json" -d '{"test":"data"}'

# Testar o endpoint problemático
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@email.com","senha":"123456","tipo":"ATLETA","ativo":true}'
```

### Passo 3: Analisar Logs

Procure por estas mensagens nos logs:

**✅ SUCESSO:**
```
🔍 [12345] =============== INÍCIO DO REQUEST DEBUG ===============
📝 [12345] REQUEST DETAILS:
   Method: POST
   URI: /api/usuarios
🔐 [12345] SECURITY CONTEXT ANTES:
   Authentication: null
📤 [12345] RESPONSE DETAILS:
   Status: 201
```

**❌ ERRO 403:**
```
🚫 [12345] 403 FORBIDDEN - REQUEST REJEITADO PELO SPRING SECURITY!
   Possíveis causas:
   - Endpoint não está configurado como permitAll()
   - CSRF proteção ativa
   - CORS preflight request falhando
   - @PreAuthorize annotation sem authentication
```

### Passo 4: Teste com Configuração Minimal

Se ainda houver problemas:

1. Adicione no `application.properties`:
   ```properties
   security.config.minimal=true
   ```

2. Reinicie a aplicação
3. Teste novamente

Se funcionar com minimal config, o problema está na configuração principal.

## Possíveis Causas do 403

### 1. @PreAuthorize sem Authentication
- **Sintoma**: Endpoint tem `@PreAuthorize` mas não há autenticação
- **Solução**: Remover `@PreAuthorize` ou configurar authentication

### 2. CSRF Protection Ativa
- **Sintoma**: POST requests retornam 403
- **Solução**: `.csrf(csrf -> csrf.disable())`

### 3. CORS Preflight Failure
- **Sintoma**: OPTIONS request retorna 403
- **Solução**: `.requestMatchers("OPTIONS", "/**").permitAll()`

### 4. Filter Chain Order
- **Sintoma**: Chain de auth executa antes da public chain
- **Solução**: `@Order(1)` na public chain

### 5. SecurityMatcher Conflicts
- **Sintoma**: Request não matcha com public chain
- **Solução**: Verificar padrões em `.securityMatcher(...)`

### 6. Method Security Global
- **Sintoma**: `@EnableMethodSecurity` afeta todos endpoints
- **Solução**: Configurar adequadamente ou desabilitar

## Arquivos de Debug Criados

1. **SecurityDebugFilter.java** - Filtro de logging detalhado
2. **DebugController.java** - Endpoints de teste
3. **SecurityConfigMinimal.java** - Config minimal para testes
4. **test-security.ps1** - Script de teste automatizado
5. **SECURITY_DEBUG_GUIDE.md** - Este guia

## Limpeza Pós-Debug

Após resolver o problema, remover/ajustar:

1. Remover `spring.security.debug=true`
2. Reduzir níveis de logging
3. Remover DebugController.java
4. Remover SecurityDebugFilter.java
5. Remover SecurityConfigMinimal.java
6. Manter apenas SecurityConfig.java otimizado

## Comandos Úteis

```bash
# Compilar e executar
mvn clean compile spring-boot:run

# Testar endpoint específico com verbose
curl -v -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@email.com","senha":"123456","tipo":"ATLETA","ativo":true}'

# Ver logs em tempo real (se usando docker/container)
docker logs -f container-name

# Grep logs por 403 errors
grep -n "403\|FORBIDDEN" logs/application.log
```