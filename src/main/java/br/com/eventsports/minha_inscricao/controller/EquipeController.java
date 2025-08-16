package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.equipe.*;
import br.com.eventsports.minha_inscricao.service.EquipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

    @GetMapping
    public ResponseEntity<List<EquipeSummaryDTO>> getAllEquipes() {
        List<EquipeSummaryDTO> equipes = equipeService.findAll();
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> getEquipeById(@PathVariable Long id) {
        try {
            EquipeResponseDTO equipe = equipeService.findById(id);
            return ResponseEntity.ok(equipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<EquipeResponseDTO> createEquipe(@Valid @RequestBody EquipeCreateDTO equipeCreateDTO) {
        try {
            EquipeResponseDTO createdEquipe = equipeService.save(equipeCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEquipe);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/evento/{eventoId}/inscricao")
    public ResponseEntity<EquipeResponseDTO> criarEquipeParaInscricao(
            @PathVariable Long eventoId, 
            @Valid @RequestBody EquipeInscricaoDTO equipeInscricaoDTO,
            @RequestParam(required = false) Long usuarioLogadoId) {
        try {
            // TODO: Obter usuário logado do contexto de segurança
            // Por enquanto, usando o parâmetro opcional ou o primeiro da lista
            EquipeResponseDTO createdEquipe = equipeService.criarEquipeParaInscricao(eventoId, equipeInscricaoDTO, usuarioLogadoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEquipe);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> updateEquipe(@PathVariable Long id, @Valid @RequestBody EquipeUpdateDTO equipeUpdateDTO) {
        try {
            EquipeResponseDTO updatedEquipe = equipeService.update(id, equipeUpdateDTO);
            return ResponseEntity.ok(updatedEquipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEquipe(@PathVariable Long id) {
        try {
            equipeService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Equipe deletada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EquipeSummaryDTO>> searchEquipesByNome(@RequestParam String nome) {
        List<EquipeSummaryDTO> equipes = equipeService.findByNome(nome);
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<EquipeSummaryDTO>> getEquipesByEvento(@PathVariable Long eventoId) {
        List<EquipeSummaryDTO> equipes = equipeService.findByEventoId(eventoId);
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<EquipeSummaryDTO>> getEquipesByCategoria(@PathVariable Long categoriaId) {
        List<EquipeSummaryDTO> equipes = equipeService.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<EquipeSummaryDTO>> getEquipesAtivas() {
        List<EquipeSummaryDTO> equipes = equipeService.findEquipesAtivas();
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/evento/{eventoId}/completas")
    public ResponseEntity<List<EquipeSummaryDTO>> getEquipesCompletasByEvento(@PathVariable Long eventoId) {
        List<EquipeSummaryDTO> equipes = equipeService.findEquipesCompletasByEvento(eventoId);
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/atleta/{atletaId}")
    public ResponseEntity<List<EquipeSummaryDTO>> getEquipesByAtleta(@PathVariable Long atletaId) {
        List<EquipeSummaryDTO> equipes = equipeService.findEquipesByAtleta(atletaId);
        return ResponseEntity.ok(equipes);
    }

    // Exception Handler for this controller
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
