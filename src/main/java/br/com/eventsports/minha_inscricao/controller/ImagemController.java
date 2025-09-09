package br.com.eventsports.minha_inscricao.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import br.com.eventsports.minha_inscricao.service.ImagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Imagens", description = "Endpoints para gerenciamento de imagens de eventos")
public class ImagemController {

    private final ImagemService imagemService;

    @PostMapping(value = "/{eventoId}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload de imagem do evento",
        description = "Faz upload de uma imagem para o evento especificado. " +
                     "Apenas imagens JPEG, PNG e WebP são aceitas, com tamanho máximo de 5MB."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Imagem carregada com sucesso",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou evento não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<String> uploadImagem(
            @PathVariable Long eventoId,
            @RequestParam("imagem") MultipartFile arquivo) {
        
        try {
            log.info("Recebida solicitação de upload de imagem para evento ID: {}", eventoId);
            
            String imagemUrl = imagemService.uploadImagemEvento(arquivo, eventoId);
            
            log.info("Upload realizado com sucesso para evento ID: {}", eventoId);
            return ResponseEntity.ok(imagemUrl);
            
        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação no upload de imagem: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro de validação: " + e.getMessage());
            
        } catch (IOException e) {
            log.error("Erro de I/O no upload de imagem: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro interno ao processar imagem: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro inesperado no upload de imagem: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro inesperado: " + e.getMessage());
        }
    }

    @DeleteMapping("/{eventoId}/imagem")
    @Operation(
        summary = "Remove imagem do evento",
        description = "Remove a imagem associada ao evento especificado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Imagem removida com sucesso"),
        @ApiResponse(responseCode = "400", description = "Evento não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<String> removerImagem(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable Long eventoId) {
        
        try {
            log.info("Recebida solicitação de remoção de imagem para evento ID: {}", eventoId);
            
            imagemService.removerImagemEvento(eventoId);
            
            log.info("Imagem removida com sucesso para evento ID: {}", eventoId);
            return ResponseEntity.ok("Imagem removida com sucesso");
            
        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação na remoção de imagem: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro de validação: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro inesperado na remoção de imagem: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro inesperado: " + e.getMessage());
        }
    }

    @GetMapping("/imagens/{nomeArquivo}")
    @Operation(
        summary = "Servir imagem do evento",
        description = "Retorna o arquivo de imagem do evento para exibição"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Imagem encontrada",
            content = @Content(mediaType = "image/jpeg")
        ),
        @ApiResponse(responseCode = "404", description = "Imagem não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Resource> servirImagem(
            @Parameter(description = "Nome do arquivo de imagem", required = true)
            @PathVariable String nomeArquivo) {
        
        try {
            log.debug("Solicitação para servir imagem: {}", nomeArquivo);
            
            Resource resource = imagemService.carregarImagem(nomeArquivo);
            
            // Determinar o tipo de conteúdo
            String contentType = "application/octet-stream";
            String fileName = resource.getFilename();
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.toLowerCase().endsWith(".webp")) {
                    contentType = "image/webp";
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.warn("Imagem não encontrada: {}", nomeArquivo);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Erro ao servir imagem {}: {}", nomeArquivo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}