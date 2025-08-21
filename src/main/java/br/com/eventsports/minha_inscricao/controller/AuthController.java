package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.auth.LoginRequestDTO;
import br.com.eventsports.minha_inscricao.dto.auth.LoginResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import br.com.eventsports.minha_inscricao.service.Interfaces.IJwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Operações de autenticação JWT")
public class AuthController {

    private final IUsuarioService usuarioService;
    private final IJwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
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
            
            // Carregar UserDetails para gerar token JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            
            // Gerar token JWT
            String token = jwtService.generateToken(userDetails);
            
            // Registrar login no sistema
            usuarioService.registrarLogin(usuario.getId());
            
            // Criar resposta
            LoginResponseDTO response = LoginResponseDTO.builder()
                    .mensagem("Login realizado com sucesso")
                    .token(token)
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
    @Operation(summary = "Realizar logout", description = "Instrui o cliente a descartar o token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "200", description = "Instruções de logout retornadas")
    })
    public ResponseEntity<Void> logout() {
        log.info("POST /api/auth/logout - Instrução de logout JWT");
        
        // Com JWT stateless, o logout é realizado no lado cliente descartando o token
        // Opcionalmente, em implementações futuras, pode-se manter uma blacklist de tokens
        
        log.info("Logout JWT realizado - cliente deve descartar o token");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário atual", description = "Retorna os dados do usuário autenticado via JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UsuarioResponseDTO> me() {
        log.info("GET /api/auth/me - Buscando dados do usuário autenticado");
        
        try {
            // Obter autenticação do SecurityContext (configurado pelo JwtAuthenticationFilter)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Usuário não autenticado tentando acessar /me");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Extrair username do principal
            String email = authentication.getName();
            
            // Buscar dados completos do usuário
            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
            
            // Verificar se o usuário ainda está ativo
            if (!usuario.getAtivo()) {
                log.warn("Usuário inativo tentando acessar /me - email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            log.info("Dados do usuário retornados com sucesso - ID: {}, email: {}", 
                    usuario.getId(), usuario.getEmail());
            
            return ResponseEntity.ok(usuario);
            
        } catch (IllegalArgumentException e) {
            log.warn("Usuário não encontrado para o token fornecido");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro interno ao buscar dados do usuário autenticado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}