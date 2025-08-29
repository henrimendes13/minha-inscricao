package br.com.eventsports.minha_inscricao.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaCreateDTO;
import br.com.eventsports.minha_inscricao.dto.atleta.AtletaInscricaoDTO;
import br.com.eventsports.minha_inscricao.dto.atleta.AtletaResponseDTO;
import br.com.eventsports.minha_inscricao.dto.atleta.AtletaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.atleta.AtletaUpdateDTO;
import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAtletaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/atletas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AtletaController {

    private final IAtletaService atletaService;

    @GetMapping
    public ResponseEntity<List<AtletaSummaryDTO>> getAllAtletas() {
        List<AtletaSummaryDTO> atletas = atletaService.findAll();
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtletaResponseDTO> getAtletaById(@PathVariable Long id) {
        AtletaResponseDTO atleta = atletaService.findById(id);
        return ResponseEntity.ok(atleta);
    }

    @PostMapping("/evento/{eventoId}/inscricao/atletas")
    public ResponseEntity<AtletaResponseDTO> createAtletaForInscricao(@PathVariable Long eventoId,
            @Valid @RequestBody AtletaInscricaoDTO atletaInscricaoDTO,
            @RequestParam(required = false, defaultValue = "1") Long usuarioInscricaoId) {
        // TODO: Implementar autenticação real. Por enquanto, usa usuário por parâmetro
        AtletaResponseDTO createdAtleta = atletaService.criarAtletaParaInscricaoComUsuario(eventoId, atletaInscricaoDTO, usuarioInscricaoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAtleta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtletaResponseDTO> updateAtleta(@PathVariable Long id,
            @Valid @RequestBody AtletaUpdateDTO atletaUpdateDTO) {
        AtletaResponseDTO updatedAtleta = atletaService.update(id, atletaUpdateDTO);
        return ResponseEntity.ok(updatedAtleta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAtleta(@PathVariable Long id) {
        atletaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<AtletaResponseDTO> getAtletaByCpf(@PathVariable String cpf) {
        Optional<AtletaResponseDTO> atleta = atletaService.findByCpf(cpf);
        return atleta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<AtletaSummaryDTO>> searchAtletasByNome(@RequestParam String nome) {
        List<AtletaSummaryDTO> atletas = atletaService.findByNome(nome);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/genero/{genero}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByGenero(@PathVariable Genero genero) {
        List<AtletaSummaryDTO> atletas = atletaService.findByGenero(genero);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByEventoId(@PathVariable Long eventoId) {
        List<AtletaSummaryDTO> atletas = atletaService.findByEventoId(eventoId);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/evento/{eventoId}/categoria/{categoriaId}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByEventoIdAndCategoriaId(
            @PathVariable Long eventoId, 
            @PathVariable Long categoriaId) {
        List<AtletaSummaryDTO> atletas = atletaService.findByEventoIdAndCategoriaId(eventoId, categoriaId);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/evento/{eventoId}/inscricao/atletas")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasInscricaoByEvento(@PathVariable Long eventoId) {
        List<AtletaSummaryDTO> atletas = atletaService.findByEventoId(eventoId);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByEquipeId(@PathVariable Long equipeId) {
        List<AtletaSummaryDTO> atletas = atletaService.findByEquipeId(equipeId);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasAtivos() {
        List<AtletaSummaryDTO> atletas = atletaService.findAtletasAtivos();
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/com-contato-emergencia")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasComContatoEmergencia() {
        List<AtletaSummaryDTO> atletas = atletaService.findAtletasComContatoEmergencia();
        return ResponseEntity.ok(atletas);
    }

    @PatchMapping("/{id}/aceita-termos")
    public ResponseEntity<Void> updateAceitaTermos(@PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        Boolean aceitaTermos = request.get("aceitaTermos");
        if (aceitaTermos == null) {
            return ResponseEntity.badRequest().build();
        }
        atletaService.updateAceitaTermos(id, aceitaTermos);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cpf/{cpf}/exists")
    public ResponseEntity<Map<String, Boolean>> checkCpfExists(@PathVariable String cpf) {
        boolean exists = atletaService.existsByCpf(cpf);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/evento/{eventoId}/count")
    public ResponseEntity<Map<String, Long>> countAtletasAtivosByEventoId(@PathVariable Long eventoId) {
        long count = atletaService.countAtletasAtivosByEventoId(eventoId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/equipe/{equipeId}/count")
    public ResponseEntity<Map<String, Long>> countAtletasByEquipeId(@PathVariable Long equipeId) {
        long count = atletaService.countAtletasByEquipeId(equipeId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Atleta não encontrado", "message", e.getMessage()));
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