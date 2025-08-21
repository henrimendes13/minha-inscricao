# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandos de Desenvolvimento

### Build e Execução
```bash
# Executar a aplicação (com dados persistentes)
mvn spring-boot:run

# Executar com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Compilar o projeto
mvn compile

# Executar testes
mvn test

# Limpar e compilar
mvn clean compile

# Gerar JAR executável
mvn clean package

# Limpar banco de dados (remover pasta data/)
rm -rf data/
# No Windows: rmdir /s data
```

### Endpoints de Desenvolvimento
- **Aplicação**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console (usuário: `sa`, senha: vazia)
- **Health Check**: http://localhost:8080/actuator/health
- **Cache Info**: http://localhost:8080/actuator/cache
- **Metrics**: http://localhost:8080/actuator/metrics

### Banco de Dados H2
- **URL JDBC**: `jdbc:h2:file:./data/minha-inscricao-dev` (persistente)
- **Driver**: `org.h2.Driver`
- **Usuário**: `sa`
- **Senha**: (vazia)
- **Console Web**: http://localhost:8080/h2-console
- **Localização do arquivo**: `./data/minha-inscricao-dev.mv.db`

## Arquitetura da Aplicação

### Stack Tecnológico
- **Java 21** com **Spring Boot 3.5.4**
- **Spring Data JPA** com banco H2 baseado em arquivo (persistente)
- **Spring DevTools** para hot reload otimizado
- **Spring Cache** para otimização de performance
- **Lombok** para redução de boilerplate
- **OpenAPI 3** (Swagger) para documentação
- **Bean Validation** para validação de dados
- **Spring Actuator** para monitoramento

### Padrão Arquitetural
A aplicação segue o padrão de camadas típico do Spring Boot:

```
Controller → Service (Interface) → Service (Implementation) → Repository → Entity
     ↓            ↓                        ↓                      ↓         ↓
   REST         Business Logic          Data Access           Database    Domain
  Endpoints      + Caching            + Query Methods         Schema      Model
```

### Entidades Principais e Relacionamentos

**EventoEntity** é a entidade central do sistema com os seguintes relacionamentos:

- **OrganizadorEntity** (ManyToOne) - Organizador do evento
- **CategoriaEntity** (OneToMany) - Categorias do evento
- **InscricaoEntity** (OneToMany) - Inscrições no evento
- **WorkoutEntity** (OneToMany) - Workouts/exercícios do evento
- **LeaderboardEntity** (OneToMany) - Classificações do evento
- **TimelineEntity** (OneToOne) - Cronograma do evento
- **AnexoEntity** (OneToMany) - Anexos do evento
- **AtletaEntity** (OneToMany) - Atletas do evento

### Estratégia de Cache
- **Tipo**: Simple (em memória)
- **Cache Name**: `eventos`
- **Estratégias**:
  - `@Cacheable` para consultas (findById, findAll, search queries)
  - `@CachePut` para atualizações (save, update)
  - `@CacheEvict` para invalidação (delete, updates que afetam listas)

### Sistema de Papéis Flexíveis
A aplicação implementa um **sistema de papéis dinâmicos** onde o tipo do usuário é determinado automaticamente pelas suas ações:

#### **Tipos de Usuário:**
- **ATLETA** (padrão) - Novos usuários e quem se inscreve em eventos
- **ORGANIZADOR** - Usuários que criam eventos (automaticamente promovidos)
- **ADMIN** - **EXCLUSIVO para desenvolvedores** - acesso global ao sistema

#### **Lógica de Determinação:**
- **Cadastro**: Sempre como ATLETA (email, senha, nome, aceitaTermos)
- **Criou evento**: Torna-se ORGANIZADOR automaticamente  
- **ADMIN**: Apenas emails `@eventsports.com.br`, `@developer` ou `admin@sistema.com`

#### **Benefícios:**
✅ **Cadastro simplificado** - sem escolha prévia de papel  
✅ **Flexibilidade total** - usuário pode organizar E participar  
✅ **Evolução natural** - tipo baseado no comportamento  
✅ **Interface adaptativa** - funcionalidades aparecem conforme necessário

#### **Classe Utilitária:**
- `TipoUsuarioUtil.determinarTipo()` - Determina tipo dinamicamente
- `TipoUsuarioUtil.isAdmin()` - Verifica se é desenvolvedor
- `TipoUsuarioUtil.podeOrganizarEventos()` - Valida permissões

### Padrão DTO
Cada entidade principal possui DTOs específicos:
- **CreateDTO** - Para criação (POST)
- **UpdateDTO** - Para atualização (PUT)  
- **ResponseDTO** - Para resposta completa (GETById)
- **SummaryDTO** - Para listagens (GET List)

## Padrões de Desenvolvimento

### Uso do Lombok
- `@Entity` + `@Data` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`
- `@RequiredArgsConstructor` para injeção de dependência nos Services
- `@ToString(exclude = {...})` para evitar lazy loading em logs

### Validações
- `@NotNull`, `@NotBlank` nos DTOs para validação de entrada
- `@Valid` nos Controllers para ativar validação
- Validações customizadas nos Services (ex: validateDateRange)

### Relacionamentos JPA
- `FetchType.LAZY` por padrão para performance
- `CascadeType.ALL` para operações em cascata
- `@Builder.Default` para inicializar coleções vazias
- Métodos de conveniência nas entidades para gerenciar relacionamentos

### Tratamento de Exceções
- Exceções customizadas: `EventoNotFoundException`, `InvalidDateRangeException`
- `@ExceptionHandler` nos Controllers para tratamento centralizado
- Retorno de ResponseEntity com mensagens estruturadas

### Configurações de Data/Hora
- Timezone: `America/Sao_Paulo`
- Formato: ISO 8601 (sem timestamps numéricos)
- `LocalDateTime` para campos de data

## Convenções do Projeto

### Estrutura de Pacotes
```
br.com.eventsports.minha_inscricao/
├── config/          # Configurações (Cache, OpenAPI, etc.)
├── controller/      # REST Controllers
├── dto/            # Data Transfer Objects (por domínio)
├── entity/         # Entidades JPA
├── enums/          # Enumerações do domínio
├── exception/      # Exceções customizadas
├── repository/     # Repositories Spring Data
├── service/        # Camada de serviço
│   └── Interfaces/ # Interfaces dos serviços
└── util/           # Utilitários
```

### Naming Conventions
- **Entities**: `*Entity.java` (ex: `EventoEntity`)
- **DTOs**: `*CreateDTO`, `*UpdateDTO`, `*ResponseDTO`, `*SummaryDTO`
- **Services**: `I*Service` (interface) e `*Service` (implementação)
- **Controllers**: `*Controller`
- **Repositories**: `*Repository`

### Padrões de Query
- Métodos de repository seguem convenção Spring Data
- `@Query` personalizada para consultas complexas
- Queries JPQL com timezone awareness
- Cache integrado nas consultas

## Dados de Exemplo e Inicialização
- **Arquivo**: `initial-data.sql` contém dados iniciais para desenvolvimento
- **Inicialização Inteligente**: Dados são carregados apenas se o banco estiver vazio
- **Componente**: `DataInitializer` gerencia a carga inicial de dados
- **Dados inclusos**: CrossFit Games 2024, Maratona de São Paulo, Torneio de Natação Masters
- **Persistência**: Dados permanecem entre restarts da aplicação

## Hot Reload e Development Workflow

### Spring DevTools - Hot Reload
- **Configuração**: Habilitado por padrão com otimizações
- **Arquivos Excluídos**: `**/data/**`, `**/target/**`, `**/*.db`, `**/*.mv.db`, `**/*.trace.db`
- **Poll Interval**: 2 segundos para detectar mudanças
- **Quiet Period**: 1 segundo antes de recarregar

### Quando o Hot Reload Funciona
✅ **Não requer restart** (hot reload):
- Mudanças em Controllers (@RestController, @RequestMapping)
- Mudanças em Services (lógica de negócio)
- Mudanças em métodos de Repository personalizados
- Mudanças em configurações simples (@Value, @ConfigurationProperties)
- Mudanças em templates/arquivos estáticos

❌ **Requer restart** (hot reload não funciona):
- Mudanças em Entities JPA (@Entity, campos, relacionamentos)
- Mudanças em configurações de segurança (@Configuration, @Bean)
- Mudanças em dependências (pom.xml)
- Mudanças em arquivos application.properties (algumas configurações)
- Mudanças em anotações de cache (@Cacheable, @CacheEvict)

### Fluxo de Desenvolvimento Recomendado
1. **Executar**: `mvn spring-boot:run` (dados persistem automaticamente)
2. **Desenvolver**: Fazer alterações no código
3. **Testar**: Usar Swagger UI para testar endpoints
4. **Dados**: Permanecem salvos entre hot reloads
5. **Restart**: Só quando necessário (mudanças em entities, etc.)

### Reset de Dados de Desenvolvimento
```bash
# Parar a aplicação (Ctrl+C)
# Remover banco de dados
rm -rf data/
# Windows: rmdir /s data

# Reiniciar aplicação
mvn spring-boot:run
# Dados iniciais serão recarregados automaticamente
```

## Monitoramento e Debug
- **Logs SQL**: Habilitados via `spring.jpa.show-sql=true`
- **Cache Logs**: Debug level para `org.springframework.cache`
- **Actuator Endpoints**: Health, cache, metrics expostos
- **Console H2**: Para inspeção direta dos dados