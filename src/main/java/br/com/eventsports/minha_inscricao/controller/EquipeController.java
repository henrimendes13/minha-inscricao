package br.com.eventsports.minha_inscricao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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

import br.com.eventsports.minha_inscricao.dto.equipe.EquipeCreateDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeInscricaoDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeResponseDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeUpdateDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.IEquipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EquipeController {

    private final IEquipeService equipeService;

    @GetMapping
    public ResponseEntity<List<EquipeSummaryDTO>> getAllEquipes() {
        List<EquipeSummaryDTO> equipes = equipeService.findAll();
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> getEquipeById(@PathVariable Long id) {
        EquipeResponseDTO equipe = equipeService.findById(id);
        return ResponseEntity.ok(equipe);
    }


    @PostMapping("/evento/{eventoId}/inscricao")
    public ResponseEntity<EquipeResponseDTO> criarEquipeParaInscricao(@PathVariable Long eventoId,
            @Valid @RequestBody EquipeInscricaoDTO equipeInscricaoDTO,
            @RequestParam(required = false) Long usuarioLogadoId) {
        EquipeResponseDTO createdEquipe = equipeService.criarEquipeParaInscricao(eventoId, equipeInscricaoDTO, usuarioLogadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEquipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> updateEquipe(@PathVariable Long id, @Valid @RequestBody EquipeUpdateDTO equipeUpdateDTO) {
        EquipeResponseDTO updatedEquipe = equipeService.update(id, equipeUpdateDTO);
        return ResponseEntity.ok(updatedEquipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEquipe(@PathVariable Long id) {
        equipeService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Equipe deletada com sucesso"));
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
