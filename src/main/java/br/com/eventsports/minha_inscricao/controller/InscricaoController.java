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
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.inscricao.InscricaoResponseDTO;
import br.com.eventsports.minha_inscricao.dto.inscricao.InscricaoSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.inscricao.InscricaoUpdateDTO;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import br.com.eventsports.minha_inscricao.service.Interfaces.IInscricaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/inscricoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InscricaoController {

    private final IInscricaoService inscricaoService;

    @GetMapping
    public ResponseEntity<List<InscricaoSummaryDTO>> getAllInscricoes() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findAll();
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscricaoResponseDTO> getInscricaoById(@PathVariable Long id) {
        InscricaoResponseDTO inscricao = inscricaoService.findById(id);
        return ResponseEntity.ok(inscricao);
    }


    @PreAuthorize("@inscricaoSecurityService.canManageInscricao(#id, authentication.name, authentication.authorities)")
    @PutMapping("/{id}")
    public ResponseEntity<InscricaoResponseDTO> updateInscricao(@PathVariable Long id,
            @Valid @RequestBody InscricaoUpdateDTO inscricaoUpdateDTO) {
        InscricaoResponseDTO updatedInscricao = inscricaoService.update(id, inscricaoUpdateDTO);
        return ResponseEntity.ok(updatedInscricao);
    }

    @PreAuthorize("@inscricaoSecurityService.canManageInscricao(#id, authentication.name, authentication.authorities)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInscricao(@PathVariable Long id) {
        inscricaoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByEventoId(@PathVariable Long eventoId) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByEventoId(eventoId);
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByCategoriaId(@PathVariable Long categoriaId) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByEquipeId(@PathVariable Long equipeId) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByEquipeId(equipeId);
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByStatus(@PathVariable StatusInscricao status) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByStatus(status);
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/confirmadas")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesConfirmadas() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findInscricoesConfirmadas();
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesPendentes() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findInscricoesPendentes();
        return ResponseEntity.ok(inscricoes);
    }

    @GetMapping("/canceladas")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesCanceladas() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findInscricoesCanceladas();
        return ResponseEntity.ok(inscricoes);
    }

    @PreAuthorize("@inscricaoSecurityService.canManageInscricao(#id, authentication.name, authentication.authorities)")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<InscricaoResponseDTO> confirmarInscricao(@PathVariable Long id) {
        InscricaoResponseDTO inscricao = inscricaoService.confirmar(id);
        return ResponseEntity.ok(inscricao);
    }

    @PreAuthorize("@inscricaoSecurityService.canManageInscricao(#id, authentication.name, authentication.authorities)")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<InscricaoResponseDTO> cancelarInscricao(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String motivo = request.getOrDefault("motivo", "Cancelamento solicitado");
        InscricaoResponseDTO inscricao = inscricaoService.cancelar(id, motivo);
        return ResponseEntity.ok(inscricao);
    }

    @PreAuthorize("@inscricaoSecurityService.canManageInscricao(#id, authentication.name, authentication.authorities)")
    @PatchMapping("/{id}/lista-espera")
    public ResponseEntity<InscricaoResponseDTO> colocarEmListaEspera(@PathVariable Long id) {
        InscricaoResponseDTO inscricao = inscricaoService.colocarEmListaEspera(id);
        return ResponseEntity.ok(inscricao);
    }

    @GetMapping("/evento/{eventoId}/status/{status}/count")
    public ResponseEntity<Map<String, Long>> countByEventoIdAndStatus(@PathVariable Long eventoId,
            @PathVariable StatusInscricao status) {
        long count = inscricaoService.countByEventoIdAndStatus(eventoId, status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/categoria/{categoriaId}/status/{status}/count")
    public ResponseEntity<Map<String, Long>> countByCategoriaIdAndStatus(@PathVariable Long categoriaId,
            @PathVariable StatusInscricao status) {
        long count = inscricaoService.countByCategoriaIdAndStatus(categoriaId, status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("não encontrada")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Inscrição não encontrada", "message", e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Erro na operação", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}