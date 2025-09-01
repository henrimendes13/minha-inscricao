package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.auth.AdminLoginResponseDTO;
import br.com.eventsports.minha_inscricao.dto.auth.LoginRequestDTO;
import br.com.eventsports.minha_inscricao.dto.auth.LoginResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import br.com.eventsports.minha_inscricao.util.JwtUtil;
import br.com.eventsports.minha_inscricao.util.PasswordUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de sessão de usuários")
public class AuthController {

    private final IUsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Endpoint de login exclusivo para usuários ADMIN
     * Apenas o email admin@admin.com é considerado ADMIN
     */
    @Operation(
        summary = "Login de Administrador", 
        description = "Realiza autenticação de usuário administrador e retorna token JWT. " +
                     "Apenas o email admin@admin.com é aceito como administrador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "403", description = "Email não autorizado para admin")
    })
    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResponseDTO> loginAdmin(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Tentativa de login ADMIN para email: {}", request.getEmail());

        try {
            // 1. Verificar se é o email de admin especial
            if (!"admin@admin.com".equals(request.getEmail())) {
                log.warn("Tentativa de login admin com email não autorizado: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }

            // 2. Buscar usuário por email
            UsuarioEntity usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (usuario == null) {
                log.warn("Usuário admin não encontrado para email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

            // 3. Verificar se usuário está ativo
            if (!usuario.getAtivo()) {
                log.warn("Tentativa de login com usuário admin desativado: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }

            // 4. Validar senha
            if (!passwordUtil.matches(request.getSenha(), usuario.getSenha())) {
                log.warn("Senha incorreta para admin: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

            // 5. Gerar token JWT (especial para admin)
            String token = jwtUtil.generateAdminToken(usuario);

            // 6. Registrar login
            usuarioService.registrarLogin(usuario.getId());

            // 7. Criar resposta
            AdminLoginResponseDTO response = AdminLoginResponseDTO.success(
                    token, 
                    usuario.getEmail(), 
                    usuario.getNome(), 
                    usuario.getId(), 
                    jwtExpiration / 1000 // Converter para segundos
            );

            log.info("Login admin realizado com sucesso: {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.error("Erro de segurança no login admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (Exception e) {
            log.error("Erro interno no login admin: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Endpoint de login universal para qualquer tipo de usuário
     */
    @Operation(
        summary = "Login Universal", 
        description = "Realiza autenticação de qualquer tipo de usuário (ADMIN, ORGANIZADOR, ATLETA) e retorna token JWT. " +
                     "O tipo de usuário é determinado automaticamente pelo sistema baseado no registro."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "403", description = "Usuário desativado")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Tentativa de login para email: {}", request.getEmail());

        try {
            // 1. Buscar usuário por email
            UsuarioEntity usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (usuario == null) {
                log.warn("Usuário não encontrado para email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

            // 2. Verificar se usuário está ativo
            if (!usuario.getAtivo()) {
                log.warn("Tentativa de login com usuário desativado: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }

            // 3. Validar senha
            if (!passwordUtil.matches(request.getSenha(), usuario.getSenha())) {
                log.warn("Senha incorreta para usuário: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

            // 4. Gerar token JWT (universal para qualquer tipo de usuário)
            String token = jwtUtil.generateToken(usuario);

            // 5. Registrar login
            usuarioService.registrarLogin(usuario.getId());

            // 6. Criar resposta
            LoginResponseDTO response = LoginResponseDTO.success(
                    token, 
                    usuario.getEmail(), 
                    usuario.getNome(), 
                    usuario.getId(), 
                    usuario.getTipoUsuario(),
                    jwtExpiration / 1000 // Converter para segundos
            );

            log.info("Login realizado com sucesso: {} (tipo: {})", request.getEmail(), usuario.getTipoUsuario());
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.error("Erro de segurança no login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (Exception e) {
            log.error("Erro interno no login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Endpoint para obter informações do admin logado
     */
    @Operation(
        summary = "Obter dados do admin logado", 
        description = "Retorna informações do usuário administrador atualmente autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do admin retornados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou não fornecido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não é admin")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/admin/me")
    @PreAuthorize("authentication.name == 'admin@admin.com'")
    public ResponseEntity<UsuarioResponseDTO> getCurrentAdmin(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.debug("Buscando informações do admin logado: {}", email);
            
            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            log.error("Erro ao buscar informações do admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para verificar se a autenticação está funcionando
     */
    @Operation(
        summary = "Status da autenticação admin", 
        description = "Verifica se a autenticação JWT está funcionando corretamente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticação funcionando corretamente"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou não fornecido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não é admin")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/admin/status")
    @PreAuthorize("authentication.name == 'admin@admin.com'")
    public ResponseEntity<Object> adminStatus(Authentication authentication) {
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Acesso admin funcionando",
                "admin", authentication.getName(),
                "timestamp", java.time.LocalDateTime.now(),
                "authorities", authentication.getAuthorities()
        ));
    }

    /**
     * Endpoint para logout (apenas limpa o contexto)
     */
    @Operation(
        summary = "Logout de administrador", 
        description = "Realiza logout do usuário administrador. " +
                     "Como JWT é stateless, apenas registra o logout - o token ainda será válido até expirar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout registrado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou não fornecido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não é admin")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/admin/logout")
    @PreAuthorize("authentication.name == 'admin@admin.com'")
    public ResponseEntity<Object> logoutAdmin(Authentication authentication) {
        log.info("Logout do admin: {}", authentication.getName());
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Logout realizado com sucesso",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }
}