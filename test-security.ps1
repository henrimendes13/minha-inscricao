# Script PowerShell para testar os endpoints de segurança
# Execute este script após iniciar a aplicação

$baseUrl = "http://localhost:8080"

Write-Host "🧪 TESTANDO CONFIGURAÇÃO DE SEGURANÇA" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Função para fazer requests e mostrar resultado
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Description,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    Write-Host "`n🔍 TESTANDO: $Description" -ForegroundColor Cyan
    Write-Host "   Método: $Method" -ForegroundColor Yellow
    Write-Host "   URL: $Url" -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body -and $Method -ne "GET") {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "   ✅ SUCESSO - Status: 200" -ForegroundColor Green
        Write-Host "   📝 Resposta:" -ForegroundColor White
        $response | ConvertTo-Json -Depth 3 | Write-Host
        
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $statusDescription = $_.Exception.Response.StatusDescription
        
        if ($statusCode -eq 403) {
            Write-Host "   ❌ ERRO 403 FORBIDDEN!" -ForegroundColor Red
        } elseif ($statusCode -eq 404) {
            Write-Host "   ⚠️  ERRO 404 NOT FOUND" -ForegroundColor Yellow
        } else {
            Write-Host "   ❌ ERRO $statusCode - $statusDescription" -ForegroundColor Red
        }
        
        Write-Host "   📝 Detalhes do erro:" -ForegroundColor White
        Write-Host "   $($_.Exception.Message)" -ForegroundColor Gray
    }
}

# Aguardar aplicação iniciar
Write-Host "`n⏳ Aguardando aplicação iniciar..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Testar endpoints de debug
Test-Endpoint -Method "GET" -Url "$baseUrl/api/debug/public" -Description "Debug GET público"
Test-Endpoint -Method "POST" -Url "$baseUrl/api/debug/public" -Description "Debug POST público" -Body '{"test": "data"}'
Test-Endpoint -Method "GET" -Url "$baseUrl/api/debug/security-context" -Description "Contexto de segurança"

# Testar endpoint de usuários - O PROBLEMA ORIGINAL
$usuarioTestData = @{
    nome = "João Teste"
    email = "joao.teste@email.com"
    senha = "123456"
    tipo = "ATLETA"
    ativo = $true
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Url "$baseUrl/api/usuarios" -Description "Criação de usuário (PROBLEMA ORIGINAL)" -Body $usuarioTestData

# Testar outros endpoints públicos
Test-Endpoint -Method "GET" -Url "$baseUrl/actuator/health" -Description "Actuator Health"
Test-Endpoint -Method "GET" -Url "$baseUrl/swagger-ui.html" -Description "Swagger UI" 
Test-Endpoint -Method "GET" -Url "$baseUrl/h2-console" -Description "H2 Console"

Write-Host "`n🏁 TESTE CONCLUÍDO!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Instruções
Write-Host "`n📋 PRÓXIMOS PASSOS:" -ForegroundColor Magenta
Write-Host "1. Verifique os logs da aplicação para detalhes de debug" -ForegroundColor White
Write-Host "2. Se houver erros 403, analise os logs do Spring Security" -ForegroundColor White
Write-Host "3. Compare os resultados dos endpoints de debug vs usuários" -ForegroundColor White
Write-Host "4. Ajuste a configuração baseado nos logs detalhados" -ForegroundColor White