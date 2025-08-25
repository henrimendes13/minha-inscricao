# Docker Setup para Minha Inscrição

Este documento explica como usar o **container PostgreSQL existente** para garantir persistência de dados.

## 🚀 Início Rápido

### 1. Executar Script de Setup (Windows)
```bash
docker-setup.bat
```

### 2. Ou Manualmente

#### Iniciar o container existente:
```bash
docker start minha_inscricao
```

#### Verificar se está rodando:
```bash
docker ps | findstr minha_inscricao
```

## 📊 Container Existente

### Detalhes do Container `minha_inscricao`
- **Nome**: `minha_inscricao`
- **Imagem**: `postgres:14.1-alpine`
- **Porta**: `5433:5432`
- **Database**: `minha_inscricao`
- **Usuário**: `postgres`
- **Senha**: `postgres`
- **Volume**: Persistente (ID: b39fea702...)

### Configuração da Aplicação
- URL: `jdbc:postgresql://localhost:5433/minha_inscricao`
- Usuário: `postgres`
- Senha: `postgres`
- Profile: `default` (application.properties)

### Para Aplicação em Docker (alternativo)
- URL: `jdbc:postgresql://host.docker.internal:5433/minha_inscricao`
- Profile: `docker` (application-docker.properties)

## 🔧 Problemas Resolvidos

### ✅ Persistência de Dados
- **Antes**: `create-drop` recriava tabelas a cada inicialização
- **Agora**: `update` mantém dados existentes
- **Volume Docker**: `postgres_data` persiste dados entre containers

### ✅ Dados Iniciais Controlados
- **Antes**: `spring.sql.init.mode=always` sobrescrevia alterações
- **Agora**: Flyway gerencia migrações de forma controlada
- **Migração**: `V2__Insert_initial_data.sql` com `ON CONFLICT DO NOTHING`

### ✅ Container Independente
- **Antes**: Banco funcionava apenas com aplicação rodando
- **Agora**: PostgreSQL em container separado com `restart: unless-stopped`

## 📝 Comandos Úteis

```bash
# Ver logs do banco
docker-compose logs postgres

# Conectar ao banco via psql
docker-compose exec postgres psql -U postgres -d minha_inscricao

# Parar todos os serviços
docker-compose down

# Resetar banco (CUIDADO: apaga todos os dados)
docker-compose down -v

# Ver status dos containers
docker-compose ps
```

## 🔄 Migrações Flyway

As migrações estão em `src/main/resources/db/migration/`:
- `V1__Create_initial_schema.sql` - Schema inicial
- `V2__Insert_initial_data.sql` - Dados iniciais
- `V3__Fix_evento_organizador_relationship.sql` - Correções

Flyway executa automaticamente na inicialização e mantém controle de versão.

## ⚠️ Notas Importantes

1. **Primeira execução**: Execute `docker-setup.bat` ou inicie o PostgreSQL antes da aplicação
2. **Dados existentes**: São preservados entre restarts da aplicação
3. **Volume persistente**: Dados ficam salvos em `minha-inscricao_postgres_data`
4. **Perfis**: Use `docker` para container completo, `default` para desenvolvimento local