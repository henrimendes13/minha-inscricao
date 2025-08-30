package br.com.eventsports.minha_inscricao.config;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserSetup {

    private final UsuarioRepository usuarioRepository;
    private final PasswordUtil passwordUtil;

    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_NAME = "Administrador do Sistema";

    /**
     * Cria o usuário admin automaticamente quando a aplicação inicia
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createAdminUser() {
        try {
            // Verificar se o usuário admin já existe
            if (usuarioRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
                log.info("Usuário admin já existe: {}", ADMIN_EMAIL);
                return;
            }

            // Criar usuário admin
            UsuarioEntity admin = UsuarioEntity.builder()
                    .email(ADMIN_EMAIL)
                    .senha(passwordUtil.encode(ADMIN_PASSWORD))
                    .nome(ADMIN_NAME)
                    .ativo(true)
                    .aceitaTermos(true)
                    .verificado(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            usuarioRepository.save(admin);
            
            log.info("========================================");
            log.info("✅ USUÁRIO ADMIN CRIADO COM SUCESSO!");
            log.info("📧 Email: {}", ADMIN_EMAIL);
            log.info("🔑 Senha: {}", ADMIN_PASSWORD);
            log.info("🛡️  Tipo: ADMIN (acesso total)");
            log.info("🔐 Este usuário NÃO pode ser criado via API");
            log.info("========================================");

        } catch (Exception e) {
            log.error("Erro ao criar usuário admin: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica se um email é o admin especial
     */
    public static boolean isAdminEmail(String email) {
        return ADMIN_EMAIL.equals(email);
    }

    /**
     * Retorna o email do admin (para uso em outros componentes)
     */
    public static String getAdminEmail() {
        return ADMIN_EMAIL;
    }
}