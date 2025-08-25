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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.eventsports.minha_inscricao.entity.AnexoEntity;
import br.com.eventsports.minha_inscricao.service.AnexoService.EstatisticasAnexo;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAnexoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/anexos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AnexoController {
    
    private final IAnexoService anexoService;
    
    @PostMapping("/upload")
    public ResponseEntity<AnexoEntity> uploadAnexo(@RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("eventoId") Long eventoId,
            @RequestParam(value = "descricao", required = false) String descricao) {
        
        try {
            log.info("Upload de anexo iniciado - Evento: {}, Arquivo: {}", eventoId, arquivo.getOriginalFilename());
            AnexoEntity anexo = anexoService.salvarAnexo(arquivo, eventoId, descricao);
            return ResponseEntity.status(HttpStatus.CREATED).body(anexo);
        } catch (IOException e) {
            log.error("Erro de I/O no upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<AnexoEntity>> listarAnexosDoEvento(@PathVariable Long eventoId) {
        List<AnexoEntity> anexos = anexoService.buscarAnexosDoEvento(eventoId);
        return ResponseEntity.ok(anexos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AnexoEntity> buscarAnexo(@PathVariable Long id) {
        return anexoService.buscarPorId(id)
            .map(anexo -> ResponseEntity.ok(anexo))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> baixarArquivo(@PathVariable Long id) {
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
    public ResponseEntity<AnexoEntity> atualizarDescricao(@PathVariable Long id, @RequestBody String novaDescricao) {
        AnexoEntity anexo = anexoService.atualizarDescricao(id, novaDescricao);
        return ResponseEntity.ok(anexo);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<AnexoEntity> alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        AnexoEntity anexo = anexoService.alterarStatus(id, ativo);
        return ResponseEntity.ok(anexo);
    }
    
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
    public ResponseEntity<List<AnexoEntity>> buscarPorTipo(@PathVariable String tipo) {
        List<AnexoEntity> anexos = anexoService.buscarPorTipo(tipo);
        return ResponseEntity.ok(anexos);
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}