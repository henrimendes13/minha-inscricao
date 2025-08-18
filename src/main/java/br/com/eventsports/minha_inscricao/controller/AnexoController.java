package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.entity.AnexoEntity;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAnexoService;
import br.com.eventsports.minha_inscricao.service.AnexoService.EstatisticasAnexo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/anexos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Anexos", description = "API para gerenciamento de anexos de eventos")
public class AnexoController {
    
    private final IAnexoService anexoService;
    
    @PostMapping("/upload")
    @Operation(summary = "Fazer upload de anexo", 
               description = "Faz upload de um arquivo anexo para um evento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Anexo criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou arquivo não permitido"),
        @ApiResponse(responseCode = "413", description = "Arquivo muito grande"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<AnexoEntity> uploadAnexo(
            @Parameter(description = "Arquivo a ser enviado", required = true)
            @RequestParam("arquivo") MultipartFile arquivo,
            
            @Parameter(description = "ID do evento", required = true, example = "1")
            @RequestParam("eventoId") Long eventoId,
            
            @Parameter(description = "Descrição opcional do anexo", example = "Regulamento da competição")
            @RequestParam(value = "descricao", required = false) String descricao) {
        
        try {
            log.info("Upload de anexo iniciado - Evento: {}, Arquivo: {}", eventoId, arquivo.getOriginalFilename());
            
            AnexoEntity anexo = anexoService.salvarAnexo(arquivo, eventoId, descricao);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(anexo);
            
        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação no upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Erro de I/O no upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/evento/{eventoId}")
    @Operation(summary = "Listar anexos de um evento", 
               description = "Retorna todos os anexos ativos de um evento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de anexos retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<List<AnexoEntity>> listarAnexosDoEvento(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId) {
        
        List<AnexoEntity> anexos = anexoService.buscarAnexosDoEvento(eventoId);
        return ResponseEntity.ok(anexos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar anexo por ID", 
               description = "Retorna os dados de um anexo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anexo encontrado"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    public ResponseEntity<AnexoEntity> buscarAnexo(
            @Parameter(description = "ID do anexo", required = true, example = "1")
            @PathVariable Long id) {
        
        return anexoService.buscarPorId(id)
            .map(anexo -> ResponseEntity.ok(anexo))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/download")
    @Operation(summary = "Baixar arquivo anexo", 
               description = "Faz download do arquivo anexo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo baixado com sucesso", 
                    content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro ao acessar arquivo")
    })
    public ResponseEntity<Resource> baixarArquivo(
            @Parameter(description = "ID do anexo", required = true, example = "1")
            @PathVariable Long id) {
        
        try {
            AnexoEntity anexo = anexoService.buscarPorId(id)
                .orElse(null);
                
            if (anexo == null) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = anexoService.baixarArquivo(id);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + anexo.getNomeArquivo() + "\"")
                .body(resource);
                
        } catch (IOException e) {
            log.error("Erro ao baixar arquivo do anexo {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/descricao")
    @Operation(summary = "Atualizar descrição do anexo", 
               description = "Atualiza a descrição de um anexo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Descrição atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    public ResponseEntity<AnexoEntity> atualizarDescricao(
            @Parameter(description = "ID do anexo", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "Nova descrição", required = true)
            @RequestBody String novaDescricao) {
        
        try {
            AnexoEntity anexo = anexoService.atualizarDescricao(id, novaDescricao);
            return ResponseEntity.ok(anexo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Alterar status do anexo", 
               description = "Ativa ou desativa um anexo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    public ResponseEntity<AnexoEntity> alterarStatus(
            @Parameter(description = "ID do anexo", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "Novo status (true = ativo, false = inativo)", required = true)
            @RequestParam boolean ativo) {
        
        try {
            AnexoEntity anexo = anexoService.alterarStatus(id, ativo);
            return ResponseEntity.ok(anexo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover anexo", 
               description = "Remove um anexo (soft delete - apenas desativa)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Anexo removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    public ResponseEntity<Void> removerAnexo(
            @Parameter(description = "ID do anexo", required = true, example = "1")
            @PathVariable Long id) {
        
        try {
            anexoService.removerAnexo(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/evento/{eventoId}/estatisticas")
    @Operation(summary = "Obter estatísticas de anexos", 
               description = "Retorna estatísticas dos anexos de um evento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<EstatisticasAnexo> obterEstatisticas(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId) {
        
        EstatisticasAnexo estatisticas = anexoService.obterEstatisticas(eventoId);
        return ResponseEntity.ok(estatisticas);
    }
    
    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Buscar anexos por tipo", 
               description = "Busca anexos por tipo base (image/, application/, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anexos encontrados")
    })
    public ResponseEntity<List<AnexoEntity>> buscarPorTipo(
            @Parameter(description = "Tipo base (ex: 'image', 'application')", required = true, example = "image")
            @PathVariable String tipo) {
        
        List<AnexoEntity> anexos = anexoService.buscarPorTipo(tipo);
        return ResponseEntity.ok(anexos);
    }
}
