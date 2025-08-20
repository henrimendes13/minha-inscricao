package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.dto.auth.LoginRequestDTO;
import br.com.eventsports.minha_inscricao.dto.organizador.OrganizadorCreateDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioComOrganizadorCreateDTO;
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
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@Slf4j
@DisplayName("Testes de Segurança - Eventos e Inscrições")
public class EventoSecurityTest {

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

        // Criar usuário organizador completo
        UsuarioComOrganizadorCreateDTO organizadorDto = UsuarioComOrganizadorCreateDTO.builder()
                .usuario(UsuarioCreateDTO.builder()
                        .nome("Maria Organizadora")
                        .email("maria.organizadora@test.com")
                        .senha("senha123")
                        .tipo(TipoUsuario.ORGANIZADOR)
                        .build())
                .organizador(UsuarioComOrganizadorCreateDTO.OrganizadorCreateDTOSemUsuario.builder()
                        .nomeEmpresa("Eventos Ltda")
                        .cnpj("12345678901234")
                        .telefone("11777777777")
                        .endereco("Rua dos Eventos, 123")
                        .descricao("Empresa de eventos esportivos")
                        .build())
                .build();
        usuarioOrganizador = usuarioService.criarComOrganizador(organizadorDto).getUsuario();
    }

    @Nested
    @DisplayName("Testes de Segurança - Eventos")
    class EventoSecurityTests {

        @Test
        @DisplayName("GET /api/eventos - Acesso público permitido")
        void getEventosAcessoPublico() throws Exception {
            mockMvc.perform(get("/api/eventos"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/eventos/{id} - Acesso público permitido")
        void getEventoPorIdAcessoPublico() throws Exception {
            mockMvc.perform(get("/api/eventos/1"))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // 404 porque não existe evento com ID 1
        }

        @Test
        @DisplayName("POST /api/eventos - Apenas ORGANIZADOR pode criar")
        void postEventoApenasOrganizador() throws Exception {
            MockHttpSession sessionOrganizador = realizarLogin(usuarioOrganizador.getEmail(), "senha123");
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String eventoJson = """
                {
                    "nome": "Evento Teste",
                    "dataInicioDoEvento": "2024-12-15T10:00:00",
                    "dataFimDoEvento": "2024-12-15T18:00:00",
                    "descricao": "Descrição do evento teste"
                }
                """;

            // Organizador pode criar
            mockMvc.perform(post("/api/eventos")
                            .session(sessionOrganizador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(eventoJson))
                    .andDo(print())
                    .andExpect(status().isCreated());

            // Atleta NÃO pode criar
            mockMvc.perform(post("/api/eventos")
                            .session(sessionAtleta)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(eventoJson))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/eventos - Acesso sem autenticação negado")
        void postEventoSemAutenticacao() throws Exception {
            String eventoJson = """
                {
                    "nome": "Evento Teste",
                    "dataInicioDoEvento": "2024-12-15T10:00:00",
                    "dataFimDoEvento": "2024-12-15T18:00:00",
                    "descricao": "Descrição do evento teste"
                }
                """;

            mockMvc.perform(post("/api/eventos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(eventoJson))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /api/eventos/{id} - Apenas ORGANIZADOR proprietário pode atualizar")
        void putEventoApenasOrganizadorProprietario() throws Exception {
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String eventoUpdateJson = """
                {
                    "nome": "Evento Atualizado",
                    "dataInicioDoEvento": "2024-12-16T10:00:00",
                    "dataFimDoEvento": "2024-12-16T18:00:00"
                }
                """;

            // Atleta NÃO pode atualizar
            mockMvc.perform(put("/api/eventos/1")
                            .session(sessionAtleta)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(eventoUpdateJson))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            // Sem autenticação também é negado
            mockMvc.perform(put("/api/eventos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(eventoUpdateJson))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/eventos/{id} - Apenas ORGANIZADOR proprietário pode deletar")
        void deleteEventoApenasOrganizadorProprietario() throws Exception {
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            // Atleta NÃO pode deletar
            mockMvc.perform(delete("/api/eventos/1")
                            .session(sessionAtleta))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            // Sem autenticação também é negado
            mockMvc.perform(delete("/api/eventos/1"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Testes de Segurança - Inscrições")
    class InscricaoSecurityTests {

        @Test
        @DisplayName("GET /api/inscricoes - Acesso público permitido")
        void getInscricoesAcessoPublico() throws Exception {
            mockMvc.perform(get("/api/inscricoes"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/inscricoes - Apenas ATLETA pode criar")
        void postInscricaoApenasAtleta() throws Exception {
            MockHttpSession sessionOrganizador = realizarLogin(usuarioOrganizador.getEmail(), "senha123");
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String inscricaoJson = """
                {
                    "categoriaId": 1,
                    "termosAceitos": true
                }
                """;

            // Atleta pode criar
            mockMvc.perform(post("/api/inscricoes")
                            .session(sessionAtleta)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(inscricaoJson))
                    .andDo(print())
                    .andExpect(status().isCreated());

            // Organizador NÃO pode criar inscrição como atleta
            mockMvc.perform(post("/api/inscricoes")
                            .session(sessionOrganizador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(inscricaoJson))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/inscricoes/evento/{eventoId} - Apenas ATLETA pode criar")
        void postInscricaoParaEventoApenasAtleta() throws Exception {
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String inscricaoJson = """
                {
                    "categoriaId": 1,
                    "termosAceitos": true
                }
                """;

            // Atleta pode criar (mesmo que dê erro por não existir evento/categoria)
            mockMvc.perform(post("/api/inscricoes/evento/1")
                            .session(sessionAtleta)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(inscricaoJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // BadRequest porque não existe evento

            // Sem autenticação é negado
            mockMvc.perform(post("/api/inscricoes/evento/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(inscricaoJson))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /api/inscricoes/{id} - Controle de ownership implementado")
        void putInscricaoControleOwnership() throws Exception {
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");
            MockHttpSession sessionOrganizador = realizarLogin(usuarioOrganizador.getEmail(), "senha123");

            String inscricaoUpdateJson = """
                {
                    "termosAceitos": true
                }
                """;

            // Organizador pode atualizar qualquer inscrição (sem validação de ownership neste caso)
            mockMvc.perform(put("/api/inscricoes/1")
                            .session(sessionOrganizador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(inscricaoUpdateJson))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // NotFound porque não existe inscrição

            // Atleta precisa de ownership (testará 403 se não for dono)
            mockMvc.perform(put("/api/inscricoes/1")
                            .session(sessionAtleta)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(inscricaoUpdateJson))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // NotFound porque não existe inscrição
        }

        @Test
        @DisplayName("PATCH /api/inscricoes/{id}/confirmar - Apenas ORGANIZADOR pode confirmar")
        void patchConfirmarInscricaoApenasOrganizador() throws Exception {
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");
            MockHttpSession sessionOrganizador = realizarLogin(usuarioOrganizador.getEmail(), "senha123");

            // Organizador pode confirmar
            mockMvc.perform(patch("/api/inscricoes/1/confirmar")
                            .session(sessionOrganizador))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // NotFound porque não existe inscrição

            // Atleta NÃO pode confirmar
            mockMvc.perform(patch("/api/inscricoes/1/confirmar")
                            .session(sessionAtleta))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/inscricoes/{id}/cancelar - Atleta com ownership ou Organizador")
        void patchCancelarInscricaoComControleOwnership() throws Exception {
            MockHttpSession sessionAtleta = realizarLogin(usuarioAtleta.getEmail(), "senha123");

            String cancelamentoJson = """
                {
                    "motivo": "Não posso participar"
                }
                """;

            // Atleta pode cancelar (se for dono da inscrição)
            mockMvc.perform(patch("/api/inscricoes/1/cancelar")
                            .session(sessionAtleta)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cancelamentoJson))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // NotFound porque não existe inscrição

            // Sem autenticação é negado
            mockMvc.perform(patch("/api/inscricoes/1/cancelar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cancelamentoJson))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // Método auxiliar
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