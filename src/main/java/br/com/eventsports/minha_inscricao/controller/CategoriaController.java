package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.categoria.*;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import br.com.eventsports.minha_inscricao.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "APIs para gerenciamento de categorias de eventos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar todas as categorias", description = "Retorna uma lista com todas as categorias cadastradas")
    public ResponseEntity<List<CategoriaSummaryDTO>> getAllCategorias() {
        List<CategoriaSummaryDTO> categorias = categoriaService.findAll();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID", description = "Retorna os detalhes completos de uma categoria específica")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        try {
            CategoriaResponseDTO categoria = categoriaService.findById(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/evento/{eventoId}")
    @Operation(summary = "Criar nova categoria", description = "Cria uma nova categoria para um evento específico")
    public ResponseEntity<CategoriaResponseDTO> createCategoria(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Valid @RequestBody CategoriaCreateDTO categoriaCreateDTO) {
        try {
            CategoriaResponseDTO createdCategoria = categoriaService.save(eventoId, categoriaCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoria);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza os dados de uma categoria existente")
    public ResponseEntity<CategoriaResponseDTO> updateCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @Valid @RequestBody CategoriaUpdateDTO categoriaUpdateDTO) {
        try {
            CategoriaResponseDTO updatedCategoria = categoriaService.update(id, categoriaUpdateDTO);
            return ResponseEntity.ok(updatedCategoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria", description = "Remove uma categoria do sistema")
    public ResponseEntity<Map<String, String>> deleteCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        try {
            categoriaService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Categoria deletada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/evento/{eventoId}")
    @Operation(summary = "Listar categorias por evento", description = "Retorna todas as categorias de um evento específico")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasByEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByEventoId(eventoId);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/ativas")
    @Operation(summary = "Listar categorias ativas", description = "Retorna todas as categorias ativas do sistema")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasAtivas() {
        List<CategoriaSummaryDTO> categorias = categoriaService.findCategoriasAtivas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/evento/{eventoId}/ativas")
    @Operation(summary = "Listar categorias ativas por evento", description = "Retorna todas as categorias ativas de um evento específico")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasAtivasByEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findCategoriasAtivasByEvento(eventoId);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/evento/{eventoId}/disponiveis")
    @Operation(summary = "Listar categorias disponíveis para inscrição", 
               description = "Retorna categorias ativas de um evento ativo que podem receber inscrições")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasDisponiveis(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findCategoriasDisponiveis(eventoId);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/tipo/{tipoParticipacao}")
    @Operation(summary = "Listar categorias por tipo de participação", 
               description = "Retorna categorias filtradas por tipo de participação (INDIVIDUAL ou EQUIPE)")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasByTipo(
            @Parameter(description = "Tipo de participação") @PathVariable TipoParticipacao tipoParticipacao) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByTipoParticipacao(tipoParticipacao);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/evento/{eventoId}/tipo/{tipoParticipacao}")
    @Operation(summary = "Listar categorias por evento e tipo de participação", 
               description = "Retorna categorias de um evento específico filtradas por tipo de participação")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasByEventoAndTipo(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Parameter(description = "Tipo de participação") @PathVariable TipoParticipacao tipoParticipacao) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByEventoIdAndTipoParticipacao(eventoId, tipoParticipacao);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar categorias por nome", description = "Busca categorias que contenham o texto no nome")
    public ResponseEntity<List<CategoriaSummaryDTO>> searchCategoriasByNome(
            @Parameter(description = "Texto para busca no nome") @RequestParam String nome) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByNome(nome);
        return ResponseEntity.ok(categorias);
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar categoria", description = "Ativa uma categoria para receber inscrições")
    public ResponseEntity<CategoriaResponseDTO> ativarCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        try {
            CategoriaResponseDTO categoria = categoriaService.ativar(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar categoria", description = "Desativa uma categoria, impedindo novas inscrições")
    public ResponseEntity<CategoriaResponseDTO> desativarCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        try {
            CategoriaResponseDTO categoria = categoriaService.desativar(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Exception Handlers
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Erro na operação", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}
