package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.auth.AdminLoginResponseDTO;
import br.com.eventsports.minha_inscricao.dto.auth.LoginRequestDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import br.com.eventsports.minha_inscricao.util.JwtUtil;
import br.com.eventsports.minha_inscricao.util.PasswordUtil;
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
     * Endpoint para obter informações do admin logado
     */
    @GetMapping("/admin/me")
    @PreAuthorize("hasRole('ADMIN')")
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
    @GetMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PostMapping("/admin/logout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> logoutAdmin(Authentication authentication) {
        log.info("Logout do admin: {}", authentication.getName());
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Logout realizado com sucesso",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }
}