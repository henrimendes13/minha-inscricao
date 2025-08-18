# Sistema de Anexos - Documentação

Este documento descreve como utilizar o sistema de anexos de arquivos para eventos esportivos.

## 📁 Estrutura Criada

### Arquivos Principais

1. **ArquivoConfig.java** - Configuração para upload de arquivos
2. **AnexoService.java** - Service principal com toda lógica de negócio
3. **IAnexoService.java** - Interface do service
4. **AnexoRepository.java** - Repository com queries específicas
5. **AnexoController.java** - Controller REST com endpoints
6. **application-arquivo.yml** - Configurações de propriedades

## 🔧 Configuração

### 1. Propriedades (application-arquivo.yml)

```yaml
app:
  arquivo:
    diretorio-upload: "./uploads/anexos/"
    tamanho-maximo-bytes: 10485760  # 10MB
    tipos-permitidos:
      - "application/pdf"
      - "image/jpeg"
      - "image/png"
      # ... outros tipos
    manter-nome-original: false
```

### 2. Configurações do Spring

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

## 🚀 Funcionalidades

### Upload de Arquivos

```java
@PostMapping("/api/anexos/upload")
```

**Parâmetros:**
- `arquivo` (MultipartFile) - Arquivo a ser enviado
- `eventoId` (Long) - ID do evento
- `descricao` (String, opcional) - Descrição do anexo

**Exemplo cURL:**
```bash
curl -X POST "http://localhost:8080/api/anexos/upload" \
  -F "arquivo=@regulamento.pdf" \
  -F "eventoId=1" \
  -F "descricao=Regulamento oficial"
```

### Listar Anexos de um Evento

```java
@GetMapping("/api/anexos/evento/{eventoId}")
```

### Download de Arquivo

```java
@GetMapping("/api/anexos/{id}/download")
```

### Estatísticas

```java
@GetMapping("/api/anexos/evento/{eventoId}/estatisticas")
```

Retorna:
```json
{
  "quantidadeAnexos": 5,
  "tamanhoTotalBytes": 2048576,
  "tamanhoTotalFormatado": "2.0 MB"
}
```

## 🛡️ Validações e Segurança

### Tipos de Arquivo Permitidos

- **Documentos**: PDF, DOC, DOCX, XLS, XLSX, PPTX, TXT
- **Imagens**: JPG, JPEG, PNG, GIF
- **Comprimidos**: ZIP, RAR

### Validações Implementadas

1. **Tamanho máximo**: 10MB por arquivo (configurável)
2. **Tipo MIME**: Validação por Content-Type
3. **Extensão**: Validação adicional por extensão do arquivo
4. **Duplicatas**: Verificação por hash MD5
5. **Integridade**: Checksum MD5 para verificar corrupção

### Segurança

- Nomes únicos gerados automaticamente
- Validação de evento existente
- Soft delete (anexos desativados, não removidos)
- Path traversal protection

## 🗄️ Estrutura do Banco

### Campos da Tabela `anexos`

```sql
CREATE TABLE anexos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome_arquivo VARCHAR(500) NOT NULL,
    descricao VARCHAR(1000),
    caminho_arquivo VARCHAR(1000) NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    extensao VARCHAR(10),
    checksum_md5 VARCHAR(32),
    ativo BOOLEAN DEFAULT TRUE,
    evento_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (evento_id) REFERENCES eventos(id)
);
```

## 📖 Exemplos de Uso

### 1. Upload Básico

```javascript
const formData = new FormData();
formData.append('arquivo', fileInput.files[0]);
formData.append('eventoId', '1');
formData.append('descricao', 'Regulamento da competição');

fetch('/api/anexos/upload', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(anexo => console.log('Anexo criado:', anexo));
```

### 2. Listar Anexos com Frontend

```javascript
fetch(`/api/anexos/evento/${eventoId}`)
.then(response => response.json())
.then(anexos => {
    anexos.forEach(anexo => {
        console.log(`${anexo.nomeArquivo} - ${anexo.getTamanhoFormatado()}`);
    });
});
```

### 3. Download Programático

```javascript
function baixarAnexo(anexoId, nomeArquivo) {
    const link = document.createElement('a');
    link.href = `/api/anexos/${anexoId}/download`;
    link.download = nomeArquivo;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
```

## ⚙️ Configurações Avançadas

### Para Produção

1. **Configurar armazenamento em cloud** (AWS S3, Google Cloud Storage)
2. **Aumentar validações de segurança**
3. **Implementar rate limiting**
4. **Configurar CDN para downloads**

### Monitoramento

```java
// Estatísticas detalhadas
@GetMapping("/admin/anexos/estatisticas-gerais")
public EstatisticasGerais obterEstatisticasGerais() {
    return anexoService.obterEstatisticasGerais();
}
```

## 🔍 Troubleshooting

### Problemas Comuns

1. **"Arquivo muito grande"**
   - Verificar `app.arquivo.tamanho-maximo-bytes`
   - Verificar `spring.servlet.multipart.max-file-size`

2. **"Tipo não permitido"**
   - Verificar `app.arquivo.tipos-permitidos`
   - Verificar `app.arquivo.extensoes-permitidas`

3. **"Diretório não encontrado"**
   - Verificar permissões da pasta
   - Verificar `app.arquivo.diretorio-upload`

### Logs Úteis

```bash
# Logs de upload
tail -f logs/application.log | grep "Upload de anexo"

# Logs de erro
tail -f logs/application.log | grep "ERROR.*Anexo"
```

## 🚀 Próximos Passos

1. **Implementar thumbnails** para imagens
2. **Adicionar preview** para PDFs
3. **Implementar compressão** automática
4. **Adicionar watermark** em imagens
5. **Implementar versioning** de arquivos

---

**Nota**: Este sistema foi projetado para ser robusto, escalável e seguro. Para dúvidas ou melhorias, consulte a documentação técnica completa.
