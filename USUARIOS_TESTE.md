# Usuários de Teste

## Credenciais para Desenvolvimento

### Admin (Acesso Total)
- **Email**: admin@admin.com
- **Senha**: admin123
- **Tipo**: ADMIN
- **Acesso**: Todos os endpoints sem restrições

### Organizador
- **Email**: organizador@test.com
- **Senha**: admin123
- **Tipo**: ORGANIZADOR
- **Acesso**: Pode criar eventos, gerenciar inscrições

### Atletas
- **Email**: joao@example.com
- **Senha**: admin123
- **Tipo**: ATLETA

- **Email**: maria@example.com
- **Senha**: admin123
- **Tipo**: ATLETA

## Endpoints para Testar

### Teste do Problema Original
1. **Login como Admin**: POST `/api/auth/login` com admin@admin.com
2. **Listar Usuários**: GET `/api/usuarios?tipo=ORGANIZADOR`
3. **Verificar**: Deve retornar status 200 com lista de organizadores

### Swagger UI
- **URL**: http://localhost:8080/swagger-ui.html
- Use as credenciais acima para testar os endpoints

### H2 Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:file:./data/minha-inscricao-dev
- **User**: sa
- **Password**: (vazio)

## Senha Hash
Todas as senhas usam: `$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem`
Que corresponde à senha: **admin123**