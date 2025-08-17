package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.atleta.*;
import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.service.AtletaService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/atletas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Atletas", description = "API para gerenciamento de atletas")
public class AtletaController {

    private final AtletaService atletaService;

    @Operation(summary = "Listar todos os atletas", description = "Retorna uma lista resumida de todos os atletas cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<AtletaSummaryDTO>> getAllAtletas() {
        List<AtletaSummaryDTO> atletas = atletaService.findAll();
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Buscar atleta por ID", description = "Retorna os dados completos de um atleta específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atleta encontrado"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AtletaResponseDTO> getAtletaById(
            @Parameter(description = "ID do atleta") @PathVariable Long id) {
        try {
            AtletaResponseDTO atleta = atletaService.findById(id);
            return ResponseEntity.ok(atleta);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Criar novo atleta", description = "Cadastra um novo atleta no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Atleta criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PostMapping
    public ResponseEntity<AtletaResponseDTO> createAtleta(@Valid @RequestBody AtletaCreateDTO atletaCreateDTO) {
        try {
            AtletaResponseDTO createdAtleta = atletaService.save(atletaCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAtleta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Criar atleta para evento", description = "Cadastra um novo atleta vinculado a um evento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Atleta criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @PostMapping("/evento/{eventoId}")
    public ResponseEntity<AtletaResponseDTO> createAtletaForEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Valid @RequestBody AtletaCreateDTO atletaCreateDTO) {
        try {
            AtletaResponseDTO createdAtleta = atletaService.saveForEvento(atletaCreateDTO, eventoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAtleta);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Criar atleta para inscrição", description = "Cadastra um novo atleta vinculado a um evento e equipe específicos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Atleta criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Evento ou equipe não encontrados")
    })
    @PostMapping("/evento/{eventoId}/equipe/{equipeId}")
    public ResponseEntity<AtletaResponseDTO> createAtletaForInscricao(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Parameter(description = "ID da equipe") @PathVariable Long equipeId,
            @Valid @RequestBody AtletaCreateDTO atletaCreateDTO) {
        try {
            AtletaResponseDTO createdAtleta = atletaService.saveForInscricao(atletaCreateDTO, eventoId, equipeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAtleta);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Atualizar atleta", description = "Atualiza os dados de um atleta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atleta atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AtletaResponseDTO> updateAtleta(
            @Parameter(description = "ID do atleta") @PathVariable Long id,
            @Valid @RequestBody AtletaUpdateDTO atletaUpdateDTO) {
        try {
            AtletaResponseDTO updatedAtleta = atletaService.update(id, atletaUpdateDTO);
            return ResponseEntity.ok(updatedAtleta);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Excluir atleta", description = "Remove um atleta do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Atleta excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAtleta(
            @Parameter(description = "ID do atleta") @PathVariable Long id) {
        try {
            atletaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Buscar atleta por CPF", description = "Retorna os dados completos de um atleta pelo CPF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atleta encontrado"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<AtletaResponseDTO> getAtletaByCpf(
            @Parameter(description = "CPF do atleta") @PathVariable String cpf) {
        Optional<AtletaResponseDTO> atleta = atletaService.findByCpf(cpf);
        return atleta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar atletas por nome", description = "Busca atletas que contenham o texto no nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas encontrados")
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<AtletaSummaryDTO>> searchAtletasByNome(
            @Parameter(description = "Texto para busca no nome") @RequestParam String nome) {
        List<AtletaSummaryDTO> atletas = atletaService.findByNome(nome);
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Buscar atletas por gênero", description = "Retorna todos os atletas de um gênero específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas encontrados")
    })
    @GetMapping("/genero/{genero}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByGenero(
            @Parameter(description = "Gênero dos atletas") @PathVariable Genero genero) {
        List<AtletaSummaryDTO> atletas = atletaService.findByGenero(genero);
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Buscar atletas por evento", description = "Retorna todos os atletas vinculados a um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas encontrados")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByEventoId(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        List<AtletaSummaryDTO> atletas = atletaService.findByEventoId(eventoId);
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Buscar atletas por equipe", description = "Retorna todos os atletas de uma equipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas encontrados")
    })
    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasByEquipeId(
            @Parameter(description = "ID da equipe") @PathVariable Long equipeId) {
        List<AtletaSummaryDTO> atletas = atletaService.findByEquipeId(equipeId);
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Listar atletas ativos", description = "Retorna todos os atletas que aceitaram os termos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas ativos")
    })
    @GetMapping("/ativos")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasAtivos() {
        List<AtletaSummaryDTO> atletas = atletaService.findAtletasAtivos();
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Listar atletas com contato de emergência", description = "Retorna atletas que possuem contato de emergência cadastrado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de atletas com contato de emergência")
    })
    @GetMapping("/com-contato-emergencia")
    public ResponseEntity<List<AtletaSummaryDTO>> getAtletasComContatoEmergencia() {
        List<AtletaSummaryDTO> atletas = atletaService.findAtletasComContatoEmergencia();
        return ResponseEntity.ok(atletas);
    }

    @Operation(summary = "Atualizar aceite de termos", description = "Atualiza apenas o status de aceite dos termos de um atleta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atleta não encontrado")
    })
    @PatchMapping("/{id}/aceita-termos")
    public ResponseEntity<Void> updateAceitaTermos(
            @Parameter(description = "ID do atleta") @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean aceitaTermos = request.get("aceitaTermos");
            if (aceitaTermos == null) {
                return ResponseEntity.badRequest().build();
            }
            atletaService.updateAceitaTermos(id, aceitaTermos);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Verificar se CPF existe", description = "Verifica se já existe um atleta cadastrado com o CPF informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada")
    })
    @GetMapping("/cpf/{cpf}/exists")
    public ResponseEntity<Map<String, Boolean>> checkCpfExists(
            @Parameter(description = "CPF para verificação") @PathVariable String cpf) {
        boolean exists = atletaService.existsByCpf(cpf);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Contar atletas ativos por evento", description = "Retorna o número de atletas ativos em um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada")
    })
    @GetMapping("/evento/{eventoId}/count")
    public ResponseEntity<Map<String, Long>> countAtletasAtivosByEventoId(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        long count = atletaService.countAtletasAtivosByEventoId(eventoId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Contar atletas por equipe", description = "Retorna o número de atletas em uma equipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada")
    })
    @GetMapping("/equipe/{equipeId}/count")
    public ResponseEntity<Map<String, Long>> countAtletasByEquipeId(
            @Parameter(description = "ID da equipe") @PathVariable Long equipeId) {
        long count = atletaService.countAtletasByEquipeId(equipeId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
