# API de Gerenciamento de Inscrições para Competições

## Visão Geral

Esta é uma API REST desenvolvida com Spring Boot para gerenciar inscrições em competições esportivas. A API permite criar, consultar, atualizar e deletar eventos, com cache em memória para otimização de performance.

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Data JPA**
- **Spring Cache**
- **H2 Database** (em memória)
- **Lombok** (redução de código boilerplate)
- **Swagger/OpenAPI 3** (documentação interativa da API)
- **Maven**

## Estrutura do Projeto

```
src/main/java/br/com/eventsports/minha_inscricao/
├── config/
│   └── CacheConfig.java              # Configuração do cache
├── controller/
│   └── EventoController.java         # Endpoints REST
├── dto/
│   └── EventoDTO.java               # Data Transfer Object
├── entity/
│   └── Evento.java                  # Entidade JPA
├── exception/
│   └── EventoNotFoundException.java  # Exception customizada
├── repository/
│   └── EventoRepository.java        # Repository com cache
├── service/
│   └── EventoService.java           # Lógica de negócio
└── MinhaInscricaoApplication.java   # Classe principal
```

## Entidade Evento

A entidade `Evento` possui as seguintes propriedades:

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | Identificador único (auto incremento) |
| nome | String | Nome do evento (obrigatório, máx 200 chars) |
| data | LocalDateTime | Data e hora do evento (obrigatório) |
| inscricao | String | Informações sobre inscrições |
| timeline | String | Cronograma do evento |
| leaderboard | String | Classificação/ranking |
| workouts | String | Descrição dos exercícios/modalidades |
| anexos | String | Arquivos anexos |
| equipes | String | Informações das equipes |
| atletas | String | Lista de atletas |
| descricao | String | Descrição detalhada do evento |
| createdAt | LocalDateTime | Data de criação (automático) |
| updatedAt | LocalDateTime | Data de atualização (automático) |

## Endpoints da API

### Base URL: `http://localhost:8080/api/eventos`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/` | Lista todos os eventos |
| GET | `/{id}` | Busca evento por ID |
| POST | `/` | Cria novo evento |
| PUT | `/{id}` | Atualiza evento existente |
| DELETE | `/{id}` | Remove evento por ID |
| GET | `/search?nome={nome}` | Busca eventos por nome |
| GET | `/upcoming` | Lista eventos futuros |
| GET | `/past` | Lista eventos passados |
| GET | `/between?inicio={data}&fim={data}` | Lista eventos entre datas |

### Exemplos de Uso

#### 1. Criar um novo evento
```bash
POST /api/eventos
Content-Type: application/json

{
    "nome": "CrossFit Open 2024",
    "data": "2024-12-15T10:00:00",
    "descricao": "Competição internacional de CrossFit",
    "inscricao": "Inscrições até 01/12/2024",
    "workouts": "WOD 1: AMRAP, WOD 2: For Time",
    "atletas": "João, Maria, Pedro"
}
```

#### 2. Buscar todos os eventos
```bash
GET /api/eventos
```

#### 3. Buscar evento por ID
```bash
GET /api/eventos/1
```

#### 4. Buscar eventos por nome
```bash
GET /api/eventos/search?nome=crossfit
```

#### 5. Listar eventos futuros
```bash
GET /api/eventos/upcoming
```

## Cache

A aplicação utiliza cache em memória para otimizar consultas frequentes:

- **Cache de eventos individuais**: `eventos` (key: ID do evento)
- **Cache de listas**: `eventos` (key: critério de busca)
- **Invalidação automática**: Cache é limpo quando dados são modificados

## Configuração do Banco de Dados

### H2 Database (Desenvolvimento)
- **URL**: `jdbc:h2:mem:testdb`
- **Console**: `http://localhost:8080/h2-console`
- **Usuário**: `sa`
- **Senha**: (em branco)

### Dados Iniciais
O arquivo `data.sql` contém dados de exemplo que são carregados automaticamente:
- CrossFit Games 2024
- Maratona de São Paulo
- Torneio de Natação Masters

## Como Executar

1. **Pré-requisitos**:
   - Java 21+
   - Maven 3.6+

2. **Executar a aplicação**:
   ```bash
   mvn spring-boot:run
   ```

3. **Acessar a aplicação**:
   - **API**: `http://localhost:8080/api/eventos`
   - **Swagger UI**: `http://localhost:8080/swagger-ui.html` 📋
   - **API Docs**: `http://localhost:8080/api-docs`
   - **H2 Console**: `http://localhost:8080/h2-console`
   - **Health Check**: `http://localhost:8080/actuator/health`

## Monitoramento

### Endpoints Actuator
- `/actuator/health` - Status da aplicação
- `/actuator/cache` - Informações do cache
- `/actuator/metrics` - Métricas da aplicação

### Logs
- Queries SQL são logadas no console
- Cache operations são logadas em DEBUG
- Timezone configurado para America/Sao_Paulo

## Tratamento de Erros

A API retorna respostas estruturadas para erros:

```json
{
    "error": "Evento não encontrado",
    "message": "Evento não encontrado com ID: 999"
}
```

### Códigos de Status
- `200 OK` - Sucesso
- `201 Created` - Evento criado
- `404 Not Found` - Evento não encontrado
- `500 Internal Server Error` - Erro interno

## 📋 **Swagger UI - Documentação Interativa**

A API possui documentação completa e interativa através do **Swagger UI**:

### 🚀 **Acesso ao Swagger**
- **URL**: `http://localhost:8080/swagger-ui.html`
- **API Docs JSON**: `http://localhost:8080/api-docs`

### 🎯 **Funcionalidades do Swagger**
- ✅ **Testar endpoints** diretamente na interface
- ✅ **Visualizar schemas** de request/response
- ✅ **Exemplos de dados** para cada campo
- ✅ **Códigos de resposta** detalhados
- ✅ **Validações** e constraints documentadas
- ✅ **Autorização** configurada (quando aplicável)

### 📖 **Documentação Completa**
Cada endpoint possui:
- **Descrição detalhada** da funcionalidade
- **Parâmetros obrigatórios** e opcionais
- **Exemplos de request/response**
- **Códigos de status HTTP** possíveis
- **Schemas de dados** com validações

### 🧪 **Como Testar com Swagger**
1. Acesse `http://localhost:8080/swagger-ui.html`
2. Escolha o endpoint desejado
3. Clique em "Try it out"
4. Preencha os parâmetros necessários
5. Execute a requisição
6. Visualize a resposta em tempo real

### 📋 **Exemplo de Teste**
```json
// POST /api/eventos - Criar novo evento
{
  "nome": "Copa de Futebol 2024",
  "data": "2024-12-20T15:00:00",
  "descricao": "Campeonato regional de futebol",
  "inscricao": "Inscrições até 15/12/2024",
  "workouts": "90 minutos por partida",
  "atletas": "22 jogadores por equipe"
}
```

## Validações

- **Nome**: Obrigatório, máximo 200 caracteres
- **Data**: Obrigatória, formato ISO 8601
- **Outros campos**: Opcionais, sem limite de tamanho (TEXT)

## Performance

- **Cache em memória** para consultas frequentes
- **Lazy loading** para operações de banco
- **Transações** para operações de escrita
- **Paginação** suportada (endpoints preparados)

## Otimizações com Lombok

O projeto utiliza **Lombok** para reduzir significativamente o código boilerplate:

### Anotações Utilizadas:
- `@Data` - Gera getters, setters, equals, hashCode e toString automaticamente
- `@NoArgsConstructor` - Gera construtor sem argumentos
- `@AllArgsConstructor` - Gera construtor com todos os argumentos
- `@Builder` - Implementa o padrão Builder para criação de objetos
- `@RequiredArgsConstructor` - Gera construtor para campos final (injeção de dependência)
- `@ToString(exclude = {...})` - ToString customizado excluindo campos grandes

### Benefícios:
- **Redução de ~80% do código** em entidades e DTOs
- **Manutenibilidade** - Mudanças nos campos são refletidas automaticamente
- **Legibilidade** - Foco na lógica de negócio ao invés de getters/setters
- **Menos erros** - Implementações automáticas e consistentes
- **Builder Pattern** - Criação de objetos mais legível e flexível

### Exemplo de redução de código:
```java
// Antes (sem Lombok) - ~150 linhas
public class Evento {
    private Long id;
    private String nome;
    // ... outros campos
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... todos os getters/setters, constructors, toString, equals, hashCode
}

// Depois (com Lombok) - ~30 linhas
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {
    private Long id;
    private String nome;
    // ... outros campos (getters/setters/constructors gerados automaticamente)
}
```

---

**Desenvolvido com Spring Boot 3.5.4, Java 21, Lombok e Swagger/OpenAPI 3**

## 🎯 **Links Rápidos**
- 🌐 **API Base**: `http://localhost:8080/api/eventos`
- 📋 **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- 🗄️ **H2 Console**: `http://localhost:8080/h2-console`
- ❤️ **Health Check**: `http://localhost:8080/actuator/health`
