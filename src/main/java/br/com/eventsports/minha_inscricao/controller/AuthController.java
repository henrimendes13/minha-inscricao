package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.auth.LoginRequestDTO;
import br.com.eventsports.minha_inscricao.dto.auth.LoginResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Operações de autenticação e gerenciamento de sessão")
public class AuthController {

    private final IUsuarioService usuarioService;
    
    private static final String USER_SESSION_KEY = "usuario_logado";

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e cria uma sessão HTTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest, 
                                                 HttpSession session) {
        log.info("POST /api/auth/login - Tentativa de login para email: {}", loginRequest.getEmail());
        
        try {
            // Validar credenciais
            boolean credenciaisValidas = usuarioService.validarCredenciais(loginRequest.getEmail(), loginRequest.getSenha());
            
            if (!credenciaisValidas) {
                log.warn("Credenciais inválidas para email: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Buscar dados completos do usuário
            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(loginRequest.getEmail());
            
            // Verificar se o usuário está ativo
            if (!usuario.getAtivo()) {
                log.warn("Tentativa de login com usuário inativo: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Criar sessão
            session.setAttribute(USER_SESSION_KEY, usuario.getId());
            session.setMaxInactiveInterval(3600); // 1 hora de timeout
            
            // Registrar login no sistema
            usuarioService.registrarLogin(usuario.getId());
            
            // Criar resposta
            LoginResponseDTO response = LoginResponseDTO.builder()
                    .mensagem("Login realizado com sucesso")
                    .usuario(usuario)
                    .build();
            
            log.info("Login realizado com sucesso para usuário ID: {} - email: {}", 
                    usuario.getId(), usuario.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Usuário não encontrado para email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro interno durante login para email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Realizar logout", description = "Encerra a sessão do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não está logado")
    })
    public ResponseEntity<Void> logout(HttpSession session) {
        log.info("POST /api/auth/logout - Tentativa de logout para sessão: {}", session.getId());
        
        Long usuarioId = (Long) session.getAttribute(USER_SESSION_KEY);
        
        if (usuarioId == null) {
            log.warn("Tentativa de logout sem usuário logado - sessão: {}", session.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Invalidar sessão
        session.invalidate();
        
        log.info("Logout realizado com sucesso para usuário ID: {}", usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário atual", description = "Retorna os dados do usuário logado na sessão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não está logado"),
            @ApiResponse(responseCode = "404", description = "Usuário da sessão não encontrado")
    })
    public ResponseEntity<UsuarioResponseDTO> me(HttpSession session) {
        log.info("GET /api/auth/me - Buscando dados do usuário da sessão: {}", session.getId());
        
        Long usuarioId = (Long) session.getAttribute(USER_SESSION_KEY);
        
        if (usuarioId == null) {
            log.warn("Acesso negado - usuário não logado na sessão: {}", session.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
            
            // Verificar se o usuário ainda está ativo
            if (!usuario.getAtivo()) {
                log.warn("Usuário inativo tentando acessar /me - ID: {}", usuarioId);
                session.invalidate();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            log.info("Dados do usuário retornados com sucesso - ID: {}, email: {}", 
                    usuario.getId(), usuario.getEmail());
            
            return ResponseEntity.ok(usuario);
            
        } catch (IllegalArgumentException e) {
            log.warn("Usuário da sessão não encontrado - ID: {}", usuarioId);
            session.invalidate();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro interno ao buscar dados do usuário da sessão - ID: {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}