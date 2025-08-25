@echo off
echo Starting Minha Inscricao Database Setup...
echo Using EXISTING PostgreSQL container: minha_inscricao

echo.
echo 1. Checking existing container status...
docker ps -a | findstr minha_inscricao

echo.
echo 2. Starting existing PostgreSQL container...
docker start minha_inscricao

echo.
echo 3. Waiting for database to be ready...
timeout /t 5 /nobreak

echo.
echo 4. Checking database health...
docker exec minha_inscricao pg_isready -U postgres -d minha_inscricao

echo.
echo 5. Checking existing tables...
docker exec minha_inscricao psql -U postgres -d minha_inscricao -c "\dt"

echo.
echo Database setup complete!
echo.
echo Container Details:
echo   - Name: minha_inscricao
echo   - Image: postgres:14.1-alpine  
echo   - Port: 5433:5432
echo   - Database: minha_inscricao
echo   - User: postgres / Password: postgres
echo.
echo To start the application:
echo   mvn spring-boot:run
echo.
echo To stop the database:
echo   docker stop minha_inscricao
echo.
pause