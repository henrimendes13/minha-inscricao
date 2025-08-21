package br.com.eventsports.minha_inscricao.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioComOrganizadorCreateDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioComOrganizadorResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioCreateDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioUpdateDTO;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Operações relacionadas aos usuários do sistema")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email já existe")
    })
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
        log.info("POST /api/usuarios - Criando usuário com email: {}", dto.getEmail());

        try {
            UsuarioResponseDTO usuario = usuarioService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao criar usuário: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/com-organizador")
    @Operation(summary = "Criar usuário organizador completo", description = "Cria um usuário junto com seu perfil de organizador em uma operação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário organizador criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ou CNPJ já existe")
    })
    public ResponseEntity<UsuarioComOrganizadorResponseDTO> criarComOrganizador(
            @Valid @RequestBody UsuarioComOrganizadorCreateDTO dto) {
        log.info("POST /api/usuarios/com-organizador - Criando usuário organizador completo com email: {}",
                dto.getUsuario().getEmail());

        try {
            UsuarioComOrganizadorResponseDTO resultado = usuarioService.criarComOrganizador(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao criar usuário organizador: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR') or (hasRole('ATLETA') and #id == authentication.principal.id)")
    @Operation(summary = "Buscar usuário por ID", description = "Busca um usuário específico pelo ID - Organizadores podem ver qualquer usuário, atletas apenas seu próprio perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Atletas só podem ver próprio perfil")
    })
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("GET /api/usuarios/{} - Buscando usuário por ID", id);

        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            log.warn("Usuário não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/com-organizador")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR') or (hasRole('ATLETA') and #id == authentication.principal.id)")
    @Operation(summary = "Buscar usuário com informações de organizador", description = "Busca um usuário e suas informações de organizador (se aplicável) - Organizadores podem ver qualquer usuário, atletas apenas seu próprio perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Atletas só podem ver próprio perfil")
    })
    public ResponseEntity<UsuarioComOrganizadorResponseDTO> buscarComOrganizador(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("GET /api/usuarios/{}/com-organizador - Buscando usuário com informações de organizador", id);

        try {
            UsuarioComOrganizadorResponseDTO usuario = usuarioService.buscarComOrganizador(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            log.warn("Usuário não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Buscar usuário por email", description = "Busca um usuário específico pelo email - Apenas organizadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Apenas organizadores")
    })
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(
            @Parameter(description = "Email do usuário") @PathVariable String email) {
        log.info("GET /api/usuarios/email/{} - Buscando usuário por email", email);

        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            log.warn("Usuário não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Listar usuários", description = "Lista usuários com paginação e filtros opcionais - Apenas organizadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Apenas organizadores")
    })
    public ResponseEntity<List<UsuarioSummaryDTO>> listar(
            @Parameter(description = "Tipo de usuário para filtrar") @RequestParam(required = false) TipoUsuario tipo) {

        List<UsuarioSummaryDTO> usuarios;

        if (tipo != null) {
            usuarios = usuarioService.listarPorTipo(tipo);
        } else {
            // Converter Page para List
            Page<UsuarioSummaryDTO> page = usuarioService.listarAtivos(Pageable.unpaged());
            usuarios = page.getContent();
        }

        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR') or (hasRole('ATLETA') and #id == authentication.principal.id)")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente - Organizadores podem atualizar qualquer usuário, atletas apenas a si mesmos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email já existe"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        log.info("PUT /api/usuarios/{} - Atualizando usuário", id);

        try {
            UsuarioResponseDTO usuario = usuarioService.atualizar(id, dto);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao atualizar usuário: {}", e.getMessage());
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Desativar usuário", description = "Desativa um usuário do sistema - Apenas organizadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Apenas organizadores")
    })
    public ResponseEntity<Void> desativar(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("PATCH /api/usuarios/{}/desativar - Desativando usuário", id);

        try {
            usuarioService.desativar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao desativar usuário: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Ativar usuário", description = "Ativa um usuário do sistema - Apenas organizadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário ativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Apenas organizadores")
    })
    public ResponseEntity<Void> ativar(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("PATCH /api/usuarios/{}/ativar - Ativando usuário", id);

        try {
            usuarioService.ativar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao ativar usuário: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/login")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR') or (hasRole('ATLETA') and #id == authentication.principal.id)")
    @Operation(summary = "Registrar login", description = "Registra o login de um usuário - Usuários só podem registrar próprio login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Login registrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Só pode registrar próprio login")
    })
    public ResponseEntity<Void> registrarLogin(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("POST /api/usuarios/{}/login - Registrando login", id);

        try {
            usuarioService.registrarLogin(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao registrar login: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Obter estatísticas", description = "Obtém estatísticas gerais dos usuários - Apenas organizadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Apenas organizadores")
    })
    public ResponseEntity<Object> obterEstatisticas() {
        log.info("GET /api/usuarios/estatisticas - Obtendo estatísticas");

        Object estatisticas = usuarioService.obterEstatisticas();
        return ResponseEntity.ok(estatisticas);
    }

}
