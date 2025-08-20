package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.dto.auth.LoginRequestDTO;
import br.com.eventsports.minha_inscricao.dto.auth.LoginResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioCreateDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@Slf4j
@DisplayName("Testes de Integração de Segurança")
public class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private UsuarioResponseDTO usuarioAtleta;
    private UsuarioResponseDTO usuarioOrganizador;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Criar usuários de teste
        criarUsuariosTeste();
    }

    private void criarUsuariosTeste() {
        // Criar usuário atleta
        UsuarioCreateDTO atletaDto = UsuarioCreateDTO.builder()
                .nome("João Atleta")
                .email("joao.atleta@test.com")
                .senha("senha123")
                .tipo(TipoUsuario.ATLETA)
                .build();
        usuarioAtleta = usuarioService.criar(atletaDto);

        // Criar usuário organizador
        UsuarioCreateDTO organizadorDto = UsuarioCreateDTO.builder()
                .nome("Maria Organizadora")
                .email("maria.organizadora@test.com")
                .senha("senha123")
                .tipo(TipoUsuario.ORGANIZADOR)
                .build();
        usuarioOrganizador = usuarioService.criar(organizadorDto);
    }

    @Nested
    @DisplayName("Testes de Autenticação")
    class AuthenticationTests {

        @Test
        @DisplayName("Login com credenciais válidas - Atleta")
        void loginValidoAtleta() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email(usuarioAtleta.getEmail())
                    .senha("senha123")
                    .build();

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Login realizado com sucesso"))
                    .andExpect(jsonPath("$.usuario.email").value(usuarioAtleta.getEmail()))
                    .andExpect(jsonPath("$.usuario.tipo").value("ATLETA"))
                    .andReturn();

            // Verificar se a sessão foi criada
            MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
            assertNotNull(session.getAttribute("usuario_logado"));
            assertEquals(usuarioAtleta.getId(), session.getAttribute("usuario_logado"));
        }

        @Test
        @DisplayName("Login com credenciais válidas - Organizador")
        void loginValidoOrganizador() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email(usuarioOrganizador.getEmail())
                    .senha("senha123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.usuario.tipo").value("ORGANIZADOR"));
        }

        @Test
        @DisplayName("Login com credenciais inválidas - 401")
        void loginCredenciaisInvalidas() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email(usuarioAtleta.getEmail())
                    .senha("senhaerrada")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Login com usuário inexistente - 404")
        void loginUsuarioInexistente() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email("naoexiste@test.com")
                    .senha("senha123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Login com usuário inativo - 401")
        void loginUsuarioInativo() throws Exception {
            // Desativar usuário
            usuarioService.desativar(usuarioAtleta.getId());

            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email(usuarioAtleta.getEmail())
                    .senha("senha123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Logout com sessão válida")
        void logoutValido() throws Exception {
            // Primeiro fazer login
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            // Fazer logout
            mockMvc.perform(post("/api/auth/logout")
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verificar se pode acessar endpoint protegido (deve dar 401)
            mockMvc.perform(get("/api/auth/me")
                            .session(session))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Logout sem sessão - 401")
        void logoutSemSessao() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Endpoint /me com sessão válida")
        void meComSessaoValida() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            mockMvc.perform(get("/api/auth/me")
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(usuarioAtleta.getId()))
                    .andExpect(jsonPath("$.email").value(usuarioAtleta.getEmail()));
        }

        @Test
        @DisplayName("Endpoint /me sem sessão - 401")
        void meSemSessao() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Endpoint /me com usuário inativo invalidate sessão - 401")
        void meUsuarioInativo() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            // Desativar usuário
            usuarioService.desativar(usuarioAtleta.getId());

            mockMvc.perform(get("/api/auth/me")
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Testes de Autorização nos Controllers")
    class AuthorizationTests {

        @Test
        @DisplayName("Acesso público aos endpoints de eventos (GET)")
        void acessoPublicoEventos() throws Exception {
            mockMvc.perform(get("/api/eventos"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Acesso público ao registro de usuário")
        void acessoPublicoRegistroUsuario() throws Exception {
            UsuarioCreateDTO novoUsuario = UsuarioCreateDTO.builder()
                    .nome("Novo Usuário")
                    .email("novo@test.com")
                    .senha("senha123")
                    .tipo(TipoUsuario.ATLETA)
                    .build();

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(novoUsuario)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("@PreAuthorize - Atleta pode acessar busca por ID")
        void atletaPodeAcessarBuscaPorId() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            mockMvc.perform(get("/api/usuarios/" + usuarioAtleta.getId())
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("@PreAuthorize - Organizador pode acessar busca por email")
        void organizadorPodeAcessarBuscaPorEmail() throws Exception {
            MockHttpSession session = realizarLogin(usuarioOrganizador.getEmail(), "senha123");

            mockMvc.perform(get("/api/usuarios/email/" + usuarioAtleta.getEmail())
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("@PreAuthorize - Atleta NÃO pode acessar busca por email - 403")
        void atletaNaoPodeAcessarBuscaPorEmail() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            mockMvc.perform(get("/api/usuarios/email/" + usuarioOrganizador.getEmail())
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("@PreAuthorize - Organizador pode listar usuários")
        void organizadorPodeListarUsuarios() throws Exception {
            MockHttpSession session = realizarLogin(usuarioOrganizador.getEmail(), "senha123");

            mockMvc.perform(get("/api/usuarios")
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("@PreAuthorize - Atleta NÃO pode listar usuários - 403")
        void atletaNaoPodeListarUsuarios() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            mockMvc.perform(get("/api/usuarios")
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Acesso sem autenticação a endpoint protegido - 401")
        void acessoSemAutenticacaoEndpointProtegido() throws Exception {
            mockMvc.perform(get("/api/usuarios/" + usuarioAtleta.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Testes de Ownership Controls")
    class OwnershipTests {

        @Test
        @DisplayName("Atleta pode atualizar próprios dados")
        void atletaPodeAtualizarPropriosDados() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String updateJson = """
                {
                    "nome": "João Atleta Atualizado",
                    "telefone": "11666666666"
                }
                """;

            mockMvc.perform(put("/api/usuarios/" + usuarioAtleta.getId())
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("João Atleta Atualizado"));
        }

        @Test
        @DisplayName("Atleta NÃO pode atualizar dados de outro usuário - 403")
        void atletaNaoPodeAtualizarOutrosUsuarios() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String updateJson = """
                {
                    "nome": "Tentativa de alteração",
                    "telefone": "11555555555"
                }
                """;

            mockMvc.perform(put("/api/usuarios/" + usuarioOrganizador.getId())
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Organizador pode atualizar dados de qualquer usuário")
        void organizadorPodeAtualizarQualquerUsuario() throws Exception {
            MockHttpSession session = realizarLogin(usuarioOrganizador.getEmail(), "senha123");

            String updateJson = """
                {
                    "nome": "João Atleta Atualizado pelo Organizador",
                    "telefone": "11444444444"
                }
                """;

            mockMvc.perform(put("/api/usuarios/" + usuarioAtleta.getId())
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("João Atleta Atualizado pelo Organizador"));
        }

        @Test
        @DisplayName("Apenas organizador pode desativar usuários")
        void apenasOrganizadorPodeDesativarUsuarios() throws Exception {
            MockHttpSession sessionOrganizador = realizarLogin(usuarioOrganizador.getEmail(), "senha123");
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            // Organizador pode desativar
            mockMvc.perform(patch("/api/usuarios/" + usuarioAtleta.getId() + "/desativar")
                            .session(sessionOrganizador))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Reativar para teste do atleta
            mockMvc.perform(patch("/api/usuarios/" + usuarioAtleta.getId() + "/ativar")
                            .session(sessionOrganizador))
                    .andExpect(status().isNoContent());

            // Atleta NÃO pode desativar
            mockMvc.perform(patch("/api/usuarios/" + usuarioOrganizador.getId() + "/desativar")
                            .session(sessionAtleta))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Testes de Cenários de Acesso Negado")
    class AccessDeniedTests {

        @Test
        @DisplayName("Headers de segurança estão configurados")
        void headersSegurancaConfigurados() throws Exception {
            mockMvc.perform(get("/api/auth/login"))
                    .andDo(print())
                    .andExpect(header().exists("X-Content-Type-Options"))
                    .andExpect(header().exists("X-Frame-Options"));
        }

        @Test
        @DisplayName("CORS está configurado corretamente")
        void corsConfiguradoCorretamente() throws Exception {
            mockMvc.perform(options("/api/usuarios")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "GET"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        }

        @Test
        @DisplayName("Validação de entrada com dados inválidos")
        void validacaoEntradaDadosInvalidos() throws Exception {
            LoginRequestDTO loginInvalido = LoginRequestDTO.builder()
                    .email("email-invalido")
                    .senha("123") // muito curta
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rate limiting - Múltiplas tentativas de login")
        void rateLimitingMultiplasTentativasLogin() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email("naoexiste@test.com")
                    .senha("senhaerrada")
                    .build();

            // Fazer múltiplas tentativas de login inválido
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andDo(print())
                        .andExpect(status().isNotFound());
            }

            // Esta implementação básica não tem rate limiting,
            // mas o teste documenta onde deveria estar
            log.warn("Rate limiting não implementado - considerar adicionar para segurança");
        }

        @Test
        @DisplayName("Sessão expira após inatividade")
        void sessaoExpiraAposInatividade() throws Exception {
            MockHttpSession session = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            // Simular expiração da sessão
            session.setMaxInactiveInterval(1); // 1 segundo
            Thread.sleep(2000); // Esperar 2 segundos

            mockMvc.perform(get("/api/auth/me")
                            .session(session))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // Métodos auxiliares

    private MockHttpSession realizarLogin(String email, String senha) throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession();
    }
}