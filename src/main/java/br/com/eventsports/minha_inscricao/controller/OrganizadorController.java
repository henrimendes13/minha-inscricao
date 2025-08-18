package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.organizador.*;
import br.com.eventsports.minha_inscricao.service.OrganizadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizadores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizadores", description = "Operações relacionadas aos organizadores de eventos")
public class OrganizadorController {

    private final OrganizadorService organizadorService;

    @PostMapping
    @Operation(summary = "Criar organizador", description = "Cria um novo organizador vinculado a um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organizador criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CNPJ já existe ou usuário já é organizador")
    })
    public ResponseEntity<OrganizadorResponseDTO> criar(@Valid @RequestBody OrganizadorCreateDTO dto) {
        log.info("POST /api/organizadores - Criando organizador para usuário ID: {}", dto.getUsuarioId());
        
        try {
            OrganizadorResponseDTO organizador = organizadorService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(organizador);
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao criar organizador: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar organizador por ID", description = "Busca um organizador específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizador encontrado"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado")
    })
    public ResponseEntity<OrganizadorResponseDTO> buscarPorId(
            @Parameter(description = "ID do organizador") @PathVariable Long id) {
        log.info("GET /api/organizadores/{} - Buscando organizador por ID", id);
        
        try {
            OrganizadorResponseDTO organizador = organizadorService.buscarPorId(id);
            return ResponseEntity.ok(organizador);
        } catch (IllegalArgumentException e) {
            log.warn("Organizador não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar organizador por usuário", description = "Busca organizador pelo ID do usuário vinculado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizador encontrado"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado para este usuário")
    })
    public ResponseEntity<OrganizadorResponseDTO> buscarPorUsuarioId(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId) {
        log.info("GET /api/organizadores/usuario/{} - Buscando organizador por usuário ID", usuarioId);
        
        try {
            OrganizadorResponseDTO organizador = organizadorService.buscarPorUsuarioId(usuarioId);
            return ResponseEntity.ok(organizador);
        } catch (IllegalArgumentException e) {
            log.warn("Organizador não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cnpj/{cnpj}")
    @Operation(summary = "Buscar organizador por CNPJ", description = "Busca organizador pelo CNPJ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizador encontrado"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado com este CNPJ")
    })
    public ResponseEntity<OrganizadorResponseDTO> buscarPorCnpj(
            @Parameter(description = "CNPJ do organizador") @PathVariable String cnpj) {
        log.info("GET /api/organizadores/cnpj/{} - Buscando organizador por CNPJ", cnpj);
        
        try {
            OrganizadorResponseDTO organizador = organizadorService.buscarPorCnpj(cnpj);
            return ResponseEntity.ok(organizador);
        } catch (IllegalArgumentException e) {
            log.warn("Organizador não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Listar organizadores", description = "Lista organizadores com paginação e filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de organizadores retornada com sucesso")
    })
    public ResponseEntity<Page<OrganizadorSummaryDTO>> listar(
            @Parameter(description = "Apenas organizadores verificados") @RequestParam(required = false, defaultValue = "false") Boolean apenasVerificados,
            @Parameter(description = "Apenas que podem organizar eventos") @RequestParam(required = false, defaultValue = "false") Boolean apenasQuePodemOrganizar,
            @Parameter(description = "Nome da empresa para busca (contém)") @RequestParam(required = false) String nomeEmpresa,
            @PageableDefault(size = 20, sort = "nomeEmpresa", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("GET /api/organizadores - Listando organizadores. Verificados: {}, Podem organizar: {}, Nome: {}", 
                apenasVerificados, apenasQuePodemOrganizar, nomeEmpresa);
        
        Page<OrganizadorSummaryDTO> organizadores;
        
        if (nomeEmpresa != null && !nomeEmpresa.trim().isEmpty()) {
            if (apenasVerificados) {
                organizadores = organizadorService.buscarVerificadosPorNomeEmpresa(nomeEmpresa.trim(), pageable);
            } else {
                organizadores = organizadorService.buscarPorNomeEmpresa(nomeEmpresa.trim(), pageable);
            }
        } else if (apenasQuePodemOrganizar) {
            organizadores = organizadorService.listarQuePodemOrganizarEventos(pageable);
        } else if (apenasVerificados) {
            organizadores = organizadorService.listarVerificados(pageable);
        } else {
            // Lista todos - precisa criar método no service
            organizadores = organizadorService.listarTodos(pageable);
        }
        
        return ResponseEntity.ok(organizadores);
    }

    @GetMapping("/verificados")
    @Operation(summary = "Listar organizadores verificados", description = "Lista apenas organizadores verificados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de organizadores verificados")
    })
    public ResponseEntity<Page<OrganizadorSummaryDTO>> listarVerificados(
            @PageableDefault(size = 20, sort = "nomeEmpresa", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/organizadores/verificados - Listando organizadores verificados");
        
        Page<OrganizadorSummaryDTO> organizadores = organizadorService.listarVerificados(pageable);
        return ResponseEntity.ok(organizadores);
    }

    @GetMapping("/podem-organizar")
    @Operation(summary = "Listar organizadores que podem organizar eventos", 
               description = "Lista organizadores verificados com usuário ativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de organizadores que podem organizar eventos")
    })
    public ResponseEntity<Page<OrganizadorSummaryDTO>> listarQuePodemOrganizarEventos(
            @PageableDefault(size = 20, sort = "nomeEmpresa", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/organizadores/podem-organizar - Listando organizadores que podem organizar eventos");
        
        Page<OrganizadorSummaryDTO> organizadores = organizadorService.listarQuePodemOrganizarEventos(pageable);
        return ResponseEntity.ok(organizadores);
    }

    @GetMapping("/cidade/{cidade}")
    @Operation(summary = "Listar organizadores por cidade", description = "Lista organizadores de uma cidade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de organizadores da cidade")
    })
    public ResponseEntity<List<OrganizadorSummaryDTO>> listarPorCidade(
            @Parameter(description = "Nome da cidade") @PathVariable String cidade) {
        log.info("GET /api/organizadores/cidade/{} - Listando organizadores por cidade", cidade);
        
        List<OrganizadorSummaryDTO> organizadores = organizadorService.listarPorCidade(cidade);
        return ResponseEntity.ok(organizadores);
    }

    @GetMapping("/com-eventos")
    @Operation(summary = "Listar organizadores com eventos", description = "Lista organizadores que já criaram eventos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de organizadores com eventos")
    })
    public ResponseEntity<List<OrganizadorSummaryDTO>> listarComEventos() {
        log.info("GET /api/organizadores/com-eventos - Listando organizadores com eventos");
        
        List<OrganizadorSummaryDTO> organizadores = organizadorService.listarComEventos();
        return ResponseEntity.ok(organizadores);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar organizador", description = "Atualiza os dados de um organizador existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizador atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado"),
            @ApiResponse(responseCode = "409", description = "CNPJ já existe")
    })
    public ResponseEntity<OrganizadorResponseDTO> atualizar(
            @Parameter(description = "ID do organizador") @PathVariable Long id,
            @Valid @RequestBody OrganizadorUpdateDTO dto) {
        log.info("PUT /api/organizadores/{} - Atualizando organizador", id);
        
        try {
            OrganizadorResponseDTO organizador = organizadorService.atualizar(id, dto);
            return ResponseEntity.ok(organizador);
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao atualizar organizador: {}", e.getMessage());
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/verificar")
    @Operation(summary = "Verificar organizador", description = "Marca um organizador como verificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Organizador verificado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado")
    })
    public ResponseEntity<Void> verificar(@Parameter(description = "ID do organizador") @PathVariable Long id) {
        log.info("PATCH /api/organizadores/{}/verificar - Verificando organizador", id);
        
        try {
            organizadorService.verificar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao verificar organizador: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/remover-verificacao")
    @Operation(summary = "Remover verificação", description = "Remove a verificação de um organizador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Verificação removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado")
    })
    public ResponseEntity<Void> removerVerificacao(@Parameter(description = "ID do organizador") @PathVariable Long id) {
        log.info("PATCH /api/organizadores/{}/remover-verificacao - Removendo verificação", id);
        
        try {
            organizadorService.removerVerificacao(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao remover verificação: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/pode-organizar")
    @Operation(summary = "Verificar se pode organizar eventos", 
               description = "Verifica se o organizador pode organizar eventos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado")
    })
    public ResponseEntity<PodeOrganizarResponseDTO> podeOrganizarEventos(
            @Parameter(description = "ID do organizador") @PathVariable Long id) {
        log.info("GET /api/organizadores/{}/pode-organizar - Verificando se pode organizar eventos", id);
        
        try {
            boolean pode = organizadorService.podeOrganizarEventos(id);
            PodeOrganizarResponseDTO response = PodeOrganizarResponseDTO.builder()
                    .podeOrganizar(pode)
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao verificar se pode organizar: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas", description = "Obtém estatísticas gerais dos organizadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso")
    })
    public ResponseEntity<OrganizadorService.OrganizadorEstatisticasDTO> obterEstatisticas() {
        log.info("GET /api/organizadores/estatisticas - Obtendo estatísticas");
        
        OrganizadorService.OrganizadorEstatisticasDTO estatisticas = organizadorService.obterEstatisticas();
        return ResponseEntity.ok(estatisticas);
    }

    // DTO auxiliar para resposta de "pode organizar"
    public static class PodeOrganizarResponseDTO {
        @Parameter(description = "Indica se pode organizar eventos")
        private Boolean podeOrganizar;

        public static PodeOrganizarResponseDTOBuilder builder() {
            return new PodeOrganizarResponseDTOBuilder();
        }

        public static class PodeOrganizarResponseDTOBuilder {
            private Boolean podeOrganizar;

            public PodeOrganizarResponseDTOBuilder podeOrganizar(Boolean podeOrganizar) {
                this.podeOrganizar = podeOrganizar;
                return this;
            }

            public PodeOrganizarResponseDTO build() {
                PodeOrganizarResponseDTO dto = new PodeOrganizarResponseDTO();
                dto.podeOrganizar = this.podeOrganizar;
                return dto;
            }
        }

        // Getter
        public Boolean getPodeOrganizar() { return podeOrganizar; }
    }
}
