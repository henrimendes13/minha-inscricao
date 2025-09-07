package br.com.eventsports.minha_inscricao.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.eventsports.minha_inscricao.dto.anexo.AnexoResponseDTO;
import br.com.eventsports.minha_inscricao.dto.anexo.AnexoSummaryDTO;
import br.com.eventsports.minha_inscricao.entity.AnexoEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import br.com.eventsports.minha_inscricao.service.AnexoService.EstatisticasAnexo;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAnexoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/anexos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AnexoController {
    
    private final IAnexoService anexoService;
    
    @PreAuthorize("@anexoSecurityService.canCreateAnexoForEvento(#eventoId, authentication.name, authentication.authorities)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponseDTO> uploadAnexo(
            @RequestPart("arquivo") 
            @Schema(type = "string", format = "binary") MultipartFile arquivo,
            @RequestParam("eventoId") Long eventoId,
            @RequestParam(value = "descricao", required = false) String descricao) throws IOException {
        AnexoEntity anexo = anexoService.salvarAnexo(arquivo, eventoId, descricao);
        AnexoResponseDTO responseDTO = anexoService.toResponseDTO(anexo);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<AnexoSummaryDTO>> listarAnexosDoEvento(@PathVariable Long eventoId) {
        List<AnexoEntity> anexos = anexoService.buscarAnexosDoEvento(eventoId);
        List<AnexoSummaryDTO> anexosDTO = anexos.stream()
                .map(anexoService::toSummaryDTO)
                .toList();
        return ResponseEntity.ok(anexosDTO);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AnexoResponseDTO> buscarAnexo(@PathVariable Long id) {
        return anexoService.buscarPorId(id)
            .map(anexoService::toResponseDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> baixarArquivo(@PathVariable Long id) {
        try {
            AnexoEntity anexo = anexoService.buscarPorId(id)
                .orElse(null);
                
            if (anexo == null) {
                log.warn("Anexo não encontrado para download: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = anexoService.baixarArquivo(id);
            
            log.info("Iniciando download do anexo: {} - {} - {} bytes", 
                     anexo.getId(), anexo.getNomeArquivo(), anexo.getTamanhoBytes());
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getTipoMime()))
                .contentLength(anexo.getTamanhoBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + anexo.getNomeArquivo() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
                
        } catch (IOException e) {
            log.error("Erro ao baixar arquivo do anexo {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PreAuthorize("@anexoSecurityService.canManageAnexo(#id, authentication.name, authentication.authorities)")
    @PutMapping("/{id}/descricao")
    public ResponseEntity<AnexoResponseDTO> atualizarDescricao(@PathVariable Long id, @RequestBody String novaDescricao) {
        AnexoEntity anexo = anexoService.atualizarDescricao(id, novaDescricao);
        AnexoResponseDTO responseDTO = anexoService.toResponseDTO(anexo);
        return ResponseEntity.ok(responseDTO);
    }
    
    @PreAuthorize("@anexoSecurityService.canManageAnexo(#id, authentication.name, authentication.authorities)")
    @PutMapping("/{id}/status")
    public ResponseEntity<AnexoResponseDTO> alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        AnexoEntity anexo = anexoService.alterarStatus(id, ativo);
        AnexoResponseDTO responseDTO = anexoService.toResponseDTO(anexo);
        return ResponseEntity.ok(responseDTO);
    }
    
    @PreAuthorize("@anexoSecurityService.canManageAnexo(#id, authentication.name, authentication.authorities)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAnexo(@PathVariable Long id) {
        anexoService.removerAnexo(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/evento/{eventoId}/estatisticas")
    public ResponseEntity<EstatisticasAnexo> obterEstatisticas(@PathVariable Long eventoId) {
        EstatisticasAnexo estatisticas = anexoService.obterEstatisticas(eventoId);
        return ResponseEntity.ok(estatisticas);
    }
    
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<AnexoSummaryDTO>> buscarPorTipo(@PathVariable String tipo) {
        List<AnexoEntity> anexos = anexoService.buscarPorTipo(tipo);
        List<AnexoSummaryDTO> anexosDTO = anexos.stream()
                .map(anexoService::toSummaryDTO)
                .toList();
        return ResponseEntity.ok(anexosDTO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Anexo não encontrado", "message", e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Dados inválidos", "message", e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
        log.error("Erro de I/O no processamento de anexo", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro no processamento do arquivo", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}