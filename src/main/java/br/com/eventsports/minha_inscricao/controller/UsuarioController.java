package br.com.eventsports.minha_inscricao.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioComOrganizadorCreateDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioComOrganizadorResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioCreateDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioUpdateDTO;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
        log.info("POST /api/usuarios - Criando usuário com email: {}", dto.getEmail());
        UsuarioResponseDTO usuario = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PostMapping("/com-organizador")
    public ResponseEntity<UsuarioComOrganizadorResponseDTO> criarComOrganizador(
            @Valid @RequestBody UsuarioComOrganizadorCreateDTO dto) {
        log.info("POST /api/usuarios/com-organizador - Criando usuário organizador completo com email: {}",
                dto.getUsuario().getEmail());
        UsuarioComOrganizadorResponseDTO resultado = usuarioService.criarComOrganizador(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("GET /api/usuarios/{} - Buscando usuário por ID", id);
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/{id}/com-organizador")
    public ResponseEntity<UsuarioComOrganizadorResponseDTO> buscarComOrganizador(@PathVariable Long id) {
        log.info("GET /api/usuarios/{}/com-organizador - Buscando usuário com informações de organizador", id);
        UsuarioComOrganizadorResponseDTO usuario = usuarioService.buscarComOrganizador(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@PathVariable String email) {
        log.info("GET /api/usuarios/email/{} - Buscando usuário por email", email);
        UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioSummaryDTO>> listar(@RequestParam(required = false) TipoUsuario tipo) {
        List<UsuarioSummaryDTO> usuarios;

        if (tipo != null) {
            usuarios = usuarioService.listarPorTipo(tipo);
        } else {
            Page<UsuarioSummaryDTO> page = usuarioService.listarAtivos(Pageable.unpaged());
            usuarios = page.getContent();
        }

        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        log.info("PUT /api/usuarios/{} - Atualizando usuário", id);
        UsuarioResponseDTO usuario = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(usuario);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        log.info("PATCH /api/usuarios/{}/desativar - Desativando usuário", id);
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        log.info("PATCH /api/usuarios/{}/ativar - Ativando usuário", id);
        usuarioService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/login")
    public ResponseEntity<Void> registrarLogin(@PathVariable Long id) {
        log.info("POST /api/usuarios/{}/login - Registrando login", id);
        usuarioService.registrarLogin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<Object> obterEstatisticas() {
        log.info("GET /api/usuarios/estatisticas - Obtendo estatísticas");
        Object estatisticas = usuarioService.obterEstatisticas();
        return ResponseEntity.ok(estatisticas);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}