package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.inscricao.*;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import br.com.eventsports.minha_inscricao.service.Interfaces.IInscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscricoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Inscrições", description = "API para gerenciamento de inscrições")
public class InscricaoController {

    private final IInscricaoService inscricaoService;

    @Operation(summary = "Listar todas as inscrições", description = "Retorna uma lista resumida de todas as inscrições cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<InscricaoSummaryDTO>> getAllInscricoes() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findAll();
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Buscar inscrição por ID", description = "Retorna os dados completos de uma inscrição específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscrição encontrada"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InscricaoResponseDTO> getInscricaoById(
            @Parameter(description = "ID da inscrição") @PathVariable Long id) {
        try {
            InscricaoResponseDTO inscricao = inscricaoService.findById(id);
            return ResponseEntity.ok(inscricao);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Criar nova inscrição", description = "Cadastra uma nova inscrição no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscrição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PostMapping
    public ResponseEntity<InscricaoResponseDTO> createInscricao(@Valid @RequestBody InscricaoCreateDTO inscricaoCreateDTO) {
        try {
            InscricaoResponseDTO createdInscricao = inscricaoService.save(inscricaoCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInscricao);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Criar inscrição para evento", description = "Cadastra uma nova inscrição diretamente vinculada a um evento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscrição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @PostMapping("/evento/{eventoId}")
    public ResponseEntity<InscricaoResponseDTO> createInscricaoForEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Valid @RequestBody InscricaoCreateDTO inscricaoCreateDTO) {
        try {
            InscricaoResponseDTO createdInscricao = inscricaoService.saveForEvento(inscricaoCreateDTO, eventoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInscricao);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Criar inscrição completa com equipe e atletas", description = "Cadastra uma nova inscrição criando automaticamente a equipe e os atletas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscrição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @PostMapping("/evento/{eventoId}/completa")
    public ResponseEntity<InscricaoResponseDTO> createInscricaoCompletaForEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Valid @RequestBody InscricaoComEquipeCreateDTO inscricaoComEquipeCreateDTO) {
        try {
            InscricaoResponseDTO createdInscricao = inscricaoService.saveComEquipeForEvento(inscricaoComEquipeCreateDTO, eventoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInscricao);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Atualizar inscrição", description = "Atualiza os dados de uma inscrição existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscrição atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<InscricaoResponseDTO> updateInscricao(
            @Parameter(description = "ID da inscrição") @PathVariable Long id,
            @Valid @RequestBody InscricaoUpdateDTO inscricaoUpdateDTO) {
        try {
            InscricaoResponseDTO updatedInscricao = inscricaoService.update(id, inscricaoUpdateDTO);
            return ResponseEntity.ok(updatedInscricao);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Excluir inscrição", description = "Remove uma inscrição do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inscrição excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInscricao(
            @Parameter(description = "ID da inscrição") @PathVariable Long id) {
        try {
            inscricaoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Buscar inscrições por evento", description = "Retorna todas as inscrições de um evento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições encontradas")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByEventoId(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByEventoId(eventoId);
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Buscar inscrições por categoria", description = "Retorna todas as inscrições de uma categoria específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições encontradas")
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByCategoriaId(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Buscar inscrições por equipe", description = "Retorna todas as inscrições de uma equipe específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições encontradas")
    })
    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByEquipeId(
            @Parameter(description = "ID da equipe") @PathVariable Long equipeId) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByEquipeId(equipeId);
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Buscar inscrições por status", description = "Retorna todas as inscrições com um status específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições encontradas")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesByStatus(
            @Parameter(description = "Status da inscrição") @PathVariable StatusInscricao status) {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findByStatus(status);
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Listar inscrições confirmadas", description = "Retorna todas as inscrições confirmadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições confirmadas")
    })
    @GetMapping("/confirmadas")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesConfirmadas() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findInscricoesConfirmadas();
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Listar inscrições pendentes", description = "Retorna todas as inscrições pendentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições pendentes")
    })
    @GetMapping("/pendentes")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesPendentes() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findInscricoesPendentes();
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Listar inscrições canceladas", description = "Retorna todas as inscrições canceladas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições canceladas")
    })
    @GetMapping("/canceladas")
    public ResponseEntity<List<InscricaoSummaryDTO>> getInscricoesCanceladas() {
        List<InscricaoSummaryDTO> inscricoes = inscricaoService.findInscricoesCanceladas();
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Confirmar inscrição", description = "Confirma uma inscrição pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscrição confirmada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada"),
            @ApiResponse(responseCode = "400", description = "Inscrição não pode ser confirmada")
    })
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<InscricaoResponseDTO> confirmarInscricao(
            @Parameter(description = "ID da inscrição") @PathVariable Long id) {
        try {
            InscricaoResponseDTO inscricao = inscricaoService.confirmar(id);
            return ResponseEntity.ok(inscricao);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Cancelar inscrição", description = "Cancela uma inscrição com motivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscrição cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada"),
            @ApiResponse(responseCode = "400", description = "Inscrição não pode ser cancelada")
    })
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<InscricaoResponseDTO> cancelarInscricao(
            @Parameter(description = "ID da inscrição") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String motivo = request.getOrDefault("motivo", "Cancelamento solicitado");
            InscricaoResponseDTO inscricao = inscricaoService.cancelar(id, motivo);
            return ResponseEntity.ok(inscricao);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Colocar em lista de espera", description = "Coloca uma inscrição na lista de espera")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscrição colocada em lista de espera"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada")
    })
    @PatchMapping("/{id}/lista-espera")
    public ResponseEntity<InscricaoResponseDTO> colocarEmListaEspera(
            @Parameter(description = "ID da inscrição") @PathVariable Long id) {
        try {
            InscricaoResponseDTO inscricao = inscricaoService.colocarEmListaEspera(id);
            return ResponseEntity.ok(inscricao);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Contar inscrições por evento e status", description = "Retorna o número de inscrições de um evento com status específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada")
    })
    @GetMapping("/evento/{eventoId}/status/{status}/count")
    public ResponseEntity<Map<String, Long>> countByEventoIdAndStatus(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Parameter(description = "Status da inscrição") @PathVariable StatusInscricao status) {
        long count = inscricaoService.countByEventoIdAndStatus(eventoId, status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Contar inscrições por categoria e status", description = "Retorna o número de inscrições de uma categoria com status específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada")
    })
    @GetMapping("/categoria/{categoriaId}/status/{status}/count")
    public ResponseEntity<Map<String, Long>> countByCategoriaIdAndStatus(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId,
            @Parameter(description = "Status da inscrição") @PathVariable StatusInscricao status) {
        long count = inscricaoService.countByCategoriaIdAndStatus(categoriaId, status);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
