# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack event management and registration system for sports competitions (particularly CrossFit-style events). The application consists of:
- **Backend**: Spring Boot 3.5.4 REST API with Java 21
- **Frontend**: Angular 19 SPA with standalone components
- **Database**: PostgreSQL 14.1 (running in Docker on port 5433)

## Architecture

### Backend Structure (Spring Boot)

The backend follows a standard layered architecture:

- **Entity Layer** (`entity/`): JPA entities representing the database schema
  - Core entities: `EventoEntity`, `CategoriaEntity`, `InscricaoEntity`, `EquipeEntity`, `AtletaEntity`, `UsuarioEntity`, `WorkoutEntity`, `LeaderboardEntity`, `PagamentoEntity`, `AnexoEntity`, `TimelineEntity`

- **Repository Layer** (`repository/`): Spring Data JPA repositories for database access

- **Service Layer** (`service/`): Business logic with interface-based design
  - Interfaces in `service/Interfaces/`
  - Service caching is implemented using Spring Cache annotations (`@Cacheable`, `@CachePut`, `@CacheEvict`)
  - Key service: `EventoService` manages events with multi-level caching

- **Controller Layer** (`controller/`): REST API endpoints
  - Base path: `/api` (proxied by Angular dev server)

- **DTO Layer** (`dto/`): Organized by domain (evento, categoria, inscricao, equipe, atleta, usuario, organizador, leaderboard, workout, anexo, timeline)
  - Each domain has Create, Update, Response, and Summary DTOs

- **Configuration** (`config/`):
  - `ArquivoConfig`: File upload configuration
  - `CorsDebugConfig`: CORS configuration for development
  - Security configuration for JWT authentication

- **Enums** (`enums/`): `StatusEvento`, `StatusInscricao`, `StatusPagamento`, `FormaPagamento`, `Genero`, `TipoParticipacao`, `TipoUsuario`, `TipoWorkout`

### Frontend Structure (Angular 19)

The frontend uses Angular standalone components with lazy loading:

- **Core Module** (`core/`):
  - `auth/`: Authentication service and utilities
  - `guards/`: `AuthGuard`, `EventoOwnerGuard`
  - `interceptors/`: `AuthInterceptor` for JWT tokens
  - `services/`: HTTP services extending `BaseHttpService`
  - `constants/`: API endpoint configuration

- **Features** (`features/`): Lazy-loaded feature modules
  - `eventos/`: Event listing and details
  - `inscricoes/`: Registration management
  - `auth/`: Login functionality
  - `workout-resultados/`: Workout result management
  - `dashboard/`: Admin dashboard
  - `atletas/`, `equipes/`: Athlete and team management

- **Layout** (`layout/`): Shell components (header, footer, sidebar, main-layout)

- **Models** (`models/`): TypeScript interfaces matching backend DTOs

- **Shared** (`shared/`): Reusable components and utilities

### Database Management

- **Flyway** migrations in `src/main/resources/db/migration/`
- Migrations are applied automatically on application startup
- Configuration: `spring.flyway.baseline-on-migrate=true`, `spring.flyway.out-of-order=true`
- Database schema updates use `spring.jpa.hibernate.ddl-auto=update` for development

### Authentication & Security

- JWT-based authentication with JJWT library (v0.11.5)
- Spring Security for endpoint protection
- Admin user auto-created on startup (email: admin@admin.com, password: admin)
- Frontend: JWT stored and managed by `AuthService`, added to requests via `AuthInterceptor`

## Common Development Commands

### Backend (Spring Boot)

```bash
# Run the application (from project root)
./mvnw spring-boot:run

# Build the project
./mvnw clean install

# Run tests
./mvnw test

# Package as JAR
./mvnw clean package
```

### Frontend (Angular)

**Note**: All Angular commands must be run from the `frontend/` directory.

```bash
# Navigate to frontend directory first
cd frontend

# Install dependencies
npm install

# Start dev server (with proxy to backend on :8080)
npm start
# or
ng serve --proxy-config proxy.conf.json

# Build for production
ng build

# Build with watch mode
ng build --watch --configuration development

# Run tests
ng test

# Generate new component (standalone by default in Angular 19)
ng generate component component-name

# Generate new service
ng generate service service-name
```

### Database (PostgreSQL in Docker)

```bash
# Start existing PostgreSQL container
docker start minha_inscricao

# Check if running
docker ps | findstr minha_inscricao

# Or use docker-compose for full setup
docker-compose up -d

# Connect to database via psql
docker exec -it minha_inscricao psql -U postgres -d minha_inscricao

# View logs
docker logs minha_inscricao
```

**Database Connection Details:**
- Host: `localhost`
- Port: `5433`
- Database: `minha_inscricao`
- User: `postgres`
- Password: `postgres`

## Key Configuration Files

- `pom.xml`: Maven dependencies and build configuration
- `src/main/resources/application.properties`: Main application configuration
- `src/main/resources/application-docker.properties`: Docker-specific config
- `frontend/package.json`: npm dependencies
- `frontend/angular.json`: Angular CLI configuration
- `frontend/proxy.conf.json`: Proxy config to route `/api/*` to `localhost:8080`
- `docker-compose.yml`: Docker services configuration

## Development Workflow

1. **Start Database**: Ensure PostgreSQL container is running (`docker start minha_inscricao`)
2. **Start Backend**: Run `./mvnw spring-boot:run` from project root (starts on port 8080)
3. **Start Frontend**: Run `npm start` from `frontend/` directory (starts on port 4200 with proxy to backend)
4. **Access Application**: Navigate to `http://localhost:4200`
5. **API Documentation**: Swagger UI available at `http://localhost:8080/swagger-ui.html`

## Important Implementation Notes

### Caching Strategy
- Services use Spring Cache with cache names like `eventos-dto`
- Cache eviction happens on update/delete operations
- Cache entries use entity IDs as keys, with special 'all' key for list operations

### DTO Conversion Patterns
- Services contain private conversion methods (e.g., `convertToResponseDTO`, `convertToSummaryDTO`)
- Always convert entities to DTOs before returning from service layer
- Create/Update DTOs are converted to entities in service layer

### Transaction Management
- Service methods annotated with `@Transactional`
- Read-only operations use `@Transactional(readOnly = true)`

### Frontend State Management
- Angular services maintain state and communicate with backend
- RxJS Observables used for async operations
- Guards protect routes requiring authentication or specific roles

### File Uploads
- Configuration in `ArquivoConfig`
- Uploads directory defined in application properties
- File handling for event attachments via `AnexoEntity`

## Testing Notes

- Backend tests use JUnit 5 and Mockito
- TestContainers dependency included for integration testing
- Frontend tests use Jasmine and Karma
- Run backend tests: `./mvnw test`
- Run frontend tests: `ng test` (from frontend directory)

## Hot Reload Configuration

- **Backend**: Spring DevTools enabled with 2s poll interval
- **Frontend**: Angular CLI dev server auto-reloads on changes
- Changes to Java files trigger automatic restart
- Changes to Angular files trigger automatic browser refresh

## UI Libraries

- **Angular Material**: Material Design components (v19.2.19)
- **PrimeNG**: Rich UI component library (v19.1.4) with PrimeIcons
- **Styling**: SCSS with Azure Blue Material theme

## Additional Notes

### Package Name
- The original package name `br.com.eventsports.minha-inscricao` is invalid
- Project uses `br.com.eventsports.minha_inscricao` instead (underscores, not hyphens)

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.
