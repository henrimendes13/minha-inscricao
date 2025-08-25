package br.com.eventsports.minha_inscricao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaCreateDTO;
import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaResponseDTO;
import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaUpdateDTO;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import br.com.eventsports.minha_inscricao.service.Interfaces.ICategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoriaController {

    private final ICategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaSummaryDTO>> getAllCategorias() {
        List<CategoriaSummaryDTO> categorias = categoriaService.findAll();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(@PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.findById(id);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping("/evento/{eventoId}")
    public ResponseEntity<CategoriaResponseDTO> createCategoria(@PathVariable Long eventoId,
            @Valid @RequestBody CategoriaCreateDTO categoriaCreateDTO) {
        CategoriaResponseDTO createdCategoria = categoriaService.save(eventoId, categoriaCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> updateCategoria(@PathVariable Long id,
            @Valid @RequestBody CategoriaUpdateDTO categoriaUpdateDTO) {
        CategoriaResponseDTO updatedCategoria = categoriaService.update(id, categoriaUpdateDTO);
        return ResponseEntity.ok(updatedCategoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCategoria(@PathVariable Long id) {
        categoriaService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Categoria deletada com sucesso"));
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasByEvento(@PathVariable Long eventoId) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByEventoId(eventoId);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasAtivas() {
        List<CategoriaSummaryDTO> categorias = categoriaService.findCategoriasAtivas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/evento/{eventoId}/ativas")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasAtivasByEvento(@PathVariable Long eventoId) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findCategoriasAtivasByEvento(eventoId);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/evento/{eventoId}/disponiveis")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasDisponiveis(@PathVariable Long eventoId) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findCategoriasDisponiveis(eventoId);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/tipo/{tipoParticipacao}")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasByTipo(@PathVariable TipoParticipacao tipoParticipacao) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByTipoParticipacao(tipoParticipacao);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/evento/{eventoId}/tipo/{tipoParticipacao}")
    public ResponseEntity<List<CategoriaSummaryDTO>> getCategoriasByEventoAndTipo(@PathVariable Long eventoId,
            @PathVariable TipoParticipacao tipoParticipacao) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByEventoIdAndTipoParticipacao(eventoId, tipoParticipacao);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoriaSummaryDTO>> searchCategoriasByNome(@RequestParam String nome) {
        List<CategoriaSummaryDTO> categorias = categoriaService.findByNome(nome);
        return ResponseEntity.ok(categorias);
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<CategoriaResponseDTO> ativarCategoria(@PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.ativar(id);
        return ResponseEntity.ok(categoria);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<CategoriaResponseDTO> desativarCategoria(@PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.desativar(id);
        return ResponseEntity.ok(categoria);
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
