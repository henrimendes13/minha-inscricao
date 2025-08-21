package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.organizador.OrganizadorResponseDTO;
import br.com.eventsports.minha_inscricao.dto.usuario.*;
import br.com.eventsports.minha_inscricao.entity.OrganizadorEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.repository.OrganizadorRepository;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.util.TipoUsuarioUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizadorRepository organizadorRepository;

    /**
     * Cria um novo usuário
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponseDTO criar(UsuarioCreateDTO dto) {
        log.info("Criando novo usuário com email: {}", dto.getEmail());
        
        // Validar se email já existe
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso: " + dto.getEmail());
        }

        // Criar entidade
        UsuarioEntity usuario = UsuarioEntity.builder()
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .nome(dto.getNome())
                .aceitaTermos(dto.getAceitaTermos() != null ? dto.getAceitaTermos() : true)
                .ativo(true)
                .build();

        // Salvar
        UsuarioEntity usuarioSalvo = usuarioRepository.save(usuario);
        
        log.info("Usuário criado com sucesso. ID: {}", usuarioSalvo.getId());
        return mapToResponseDTO(usuarioSalvo);
    }

    /**
     * Cria usuário e organizador em uma operação combinada
     */
    @CacheEvict(value = {"usuarios", "organizadores"}, allEntries = true)
    public UsuarioComOrganizadorResponseDTO criarComOrganizador(UsuarioComOrganizadorCreateDTO dto) {
        log.info("Criando usuário organizador completo com email: {}", dto.getUsuario().getEmail());
        
        // Validar que dados do organizador foram fornecidos
        if (dto.getOrganizador() == null) {
            throw new IllegalArgumentException("Dados do organizador são obrigatórios para criar perfil completo de organizador");
        }

        try {
            // Criar usuário primeiro
            UsuarioResponseDTO usuario = criar(dto.getUsuario());
            
            // Validar CNPJ se informado
            if (dto.getOrganizador().getCnpj() != null && organizadorRepository.existsByCnpj(dto.getOrganizador().getCnpj())) {
                throw new IllegalArgumentException("CNPJ já está em uso: " + dto.getOrganizador().getCnpj());
            }

            // Buscar usuário criado
            UsuarioEntity usuarioEntity = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Erro interno: usuário não encontrado após criação"));

            // Criar organizador diretamente
            OrganizadorEntity organizador = OrganizadorEntity.builder()
                    .usuario(usuarioEntity)
                    .nomeEmpresa(dto.getOrganizador().getNomeEmpresa())
                    .cnpj(dto.getOrganizador().getCnpj())
                    .telefone(dto.getOrganizador().getTelefone())
                    .endereco(dto.getOrganizador().getEndereco())
                    .descricao(dto.getOrganizador().getDescricao())
                    .site(dto.getOrganizador().getSite())
                    .verificado(false)
                    .build();

            OrganizadorEntity organizadorSalvo = organizadorRepository.save(organizador);
            OrganizadorResponseDTO organizadorResponse = mapOrganizadorToResponseDTO(organizadorSalvo);
            
            log.info("Usuário organizador criado com sucesso. Usuario ID: {}, Organizador ID: {}", 
                    usuario.getId(), organizadorSalvo.getId());
            
            return UsuarioComOrganizadorResponseDTO.criarCompleto(usuario, organizadorResponse);
            
        } catch (Exception e) {
            log.error("Erro ao criar usuário organizador: {}", e.getMessage());
            throw new IllegalArgumentException("Erro ao criar perfil completo: " + e.getMessage());
        }
    }

    /**
     * Busca usuário por ID
     */
    @Cacheable(value = "usuarios", key = "#id")
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
        
        return mapToResponseDTO(usuario);
    }

    /**
     * Busca usuário por email
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        log.debug("Buscando usuário por email: {}", email);
        
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com email: " + email));
        
        return mapToResponseDTO(usuario);
    }

    /**
     * Lista todos os usuários ativos com paginação
     */
    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> listarAtivos(Pageable pageable) {
        log.debug("Listando usuários ativos com paginação");
        
        return usuarioRepository.findByAtivoTrue(pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Lista usuários por tipo
     */
    @Transactional(readOnly = true)
    public List<UsuarioSummaryDTO> listarPorTipo(TipoUsuario tipo) {
        log.debug("Listando usuários por tipo: {}", tipo);
        
        return usuarioRepository.findByAtivoTrue()
                .stream()
                .filter(usuario -> {
                    TipoUsuario tipoUsuario = usuario.getTipoUsuario();
                    return tipoUsuario.equals(tipo);
                })
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca usuários por nome
     */
    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarPorNome(String nome, Pageable pageable) {
        log.debug("Buscando usuários por nome: {}", nome);
        
        return usuarioRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Atualiza dados do usuário
     */
    @CacheEvict(value = "usuarios", key = "#id")
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        log.info("Atualizando usuário ID: {}", id);
        
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        // Validar email se foi alterado
        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email já está em uso: " + dto.getEmail());
            }
            usuario.setEmail(dto.getEmail());
        }

        // Atualizar campos se informados
        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }
        
        if (dto.getSenha() != null) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        
        if (dto.getAtivo() != null) {
            usuario.setAtivo(dto.getAtivo());
        }

        UsuarioEntity usuarioAtualizado = usuarioRepository.save(usuario);
        
        log.info("Usuário atualizado com sucesso. ID: {}", id);
        return mapToResponseDTO(usuarioAtualizado);
    }

    /**
     * Desativa um usuário
     */
    @CacheEvict(value = "usuarios", key = "#id")
    public void desativar(Long id) {
        log.info("Desativando usuário ID: {}", id);
        
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
        
        usuario.desativar();
        usuarioRepository.save(usuario);
        
        log.info("Usuário desativado com sucesso. ID: {}", id);
    }

    /**
     * Ativa um usuário
     */
    @CacheEvict(value = "usuarios", key = "#id")
    public void ativar(Long id) {
        log.info("Ativando usuário ID: {}", id);
        
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
        
        usuario.ativar();
        usuarioRepository.save(usuario);
        
        log.info("Usuário ativado com sucesso. ID: {}", id);
    }

    /**
     * Registra login do usuário
     */
    @CacheEvict(value = "usuarios", key = "#id")
    public void registrarLogin(Long id) {
        log.debug("Registrando login do usuário ID: {}", id);
        
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
        
        usuario.registrarLogin();
        usuarioRepository.save(usuario);
    }

    /**
     * Valida credenciais do usuário
     */
    @Transactional(readOnly = true)
    public boolean validarCredenciais(String email, String senha) {
        log.debug("Validando credenciais para email: {}", email);
        
        return usuarioRepository.findByEmail(email)
                .map(usuario -> usuario.getAtivo() && passwordEncoder.matches(senha, usuario.getSenha()))
                .orElse(false);
    }

    /**
     * Obtém estatísticas de usuários
     */
    @Transactional(readOnly = true)
    public UsuarioEstatisticasDTO obterEstatisticas() {
        log.debug("Obtendo estatísticas de usuários");
        
        // Contar usuários por tipo usando lógica dinâmica
        List<UsuarioEntity> usuariosAtivos = usuarioRepository.findByAtivoTrue();
        Long totalAtletas = usuariosAtivos.stream()
                .filter(usuario -> usuario.getTipoUsuario() == TipoUsuario.ATLETA)
                .count();
        Long totalOrganizadores = usuariosAtivos.stream()
                .filter(usuario -> usuario.getTipoUsuario() == TipoUsuario.ORGANIZADOR)
                .count();
        Long totalGeral = totalAtletas + totalOrganizadores;
        
        // Usuários com login recente (últimos 30 dias)
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(30);
        List<UsuarioEntity> usuariosRecentes = usuarioRepository.findUsuariosComLoginRecente(dataLimite);
        
        return UsuarioEstatisticasDTO.builder()
                .totalUsuarios(totalGeral)
                .totalAtletas(totalAtletas)
                .totalOrganizadores(totalOrganizadores)
                .usuariosAtivosUltimos30Dias((long) usuariosRecentes.size())
                .build();
    }

    /**
     * Verifica se usuário organizador tem perfil completo
     */
    @Transactional(readOnly = true)
    public boolean organizadorTemPerfilCompleto(Long usuarioId) {
        return organizadorRepository.existsByUsuarioId(usuarioId);
    }

    /**
     * Obtém resposta combinada para um usuário existente
     */
    @Transactional(readOnly = true)
    public UsuarioComOrganizadorResponseDTO buscarComOrganizador(Long usuarioId) {
        UsuarioResponseDTO usuario = buscarPorId(usuarioId);
        
        // Buscar perfil de organizador se existir
        OrganizadorEntity organizadorEntity = organizadorRepository.findByUsuarioId(usuarioId).orElse(null);
        
        if (organizadorEntity != null) {
            OrganizadorResponseDTO organizador = mapOrganizadorToResponseDTO(organizadorEntity);
            return UsuarioComOrganizadorResponseDTO.criarCompleto(usuario, organizador);
        }
        
        // Se o usuário tem tipo ORGANIZADOR mas não tem perfil, indicar necessidade de completar
        // Admins também podem precisar de perfil de organizador para criar eventos
        boolean isAdmin = usuario.getEmail() != null && (
            usuario.getEmail().contains("@eventsports.com.br") || 
            usuario.getEmail().contains("@developer") ||
            usuario.getEmail().equals("admin@sistema.com")
        );
        boolean isOrganizadorOuAdmin = TipoUsuario.ORGANIZADOR.equals(usuario.getTipo()) || isAdmin;
        if (isOrganizadorOuAdmin) {
            return UsuarioComOrganizadorResponseDTO.criarIncompleto(
                usuario, 
                "criar-organizador",
                "Complete seu perfil de organizador para poder organizar eventos."
            );
        }
        
        return UsuarioComOrganizadorResponseDTO.criarCompleto(usuario, null);
    }

    // Métodos auxiliares de mapeamento
    private UsuarioResponseDTO mapToResponseDTO(UsuarioEntity usuario) {
        // Determinar características dinâmicas
        boolean temEventos = usuario.getEventosOrganizados() != null && !usuario.getEventosOrganizados().isEmpty();
        boolean temInscricoes = usuario.getInscricoes() != null && !usuario.getInscricoes().isEmpty();
        int totalEventos = usuario.getEventosOrganizados() != null ? usuario.getEventosOrganizados().size() : 0;
        int totalInscricoes = usuario.getInscricoes() != null ? usuario.getInscricoes().size() : 0;
        
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .tipo(usuario.getTipoUsuario())
                .isOrganizador(temEventos)
                .isAtleta(temInscricoes)
                .ativo(usuario.getAtivo())
                .verificado(usuario.getVerificado())
                .totalEventos(totalEventos)
                .totalInscricoes(totalInscricoes)
                .ultimoLogin(usuario.getUltimoLogin())
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }

    private UsuarioSummaryDTO mapToSummaryDTO(UsuarioEntity usuario) {
        return UsuarioSummaryDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .tipo(usuario.getTipoUsuario())
                .ativo(usuario.getAtivo())
                .build();
    }

    private OrganizadorResponseDTO mapOrganizadorToResponseDTO(OrganizadorEntity organizador) {
        UsuarioSummaryDTO usuarioSummary = UsuarioSummaryDTO.builder()
                .id(organizador.getUsuario().getId())
                .email(organizador.getUsuario().getEmail())
                .nome(organizador.getUsuario().getNome())
                .tipo(organizador.getUsuario().getTipoUsuario())
                .ativo(organizador.getUsuario().getAtivo())
                .build();

        return OrganizadorResponseDTO.builder()
                .id(organizador.getId())
                .usuario(usuarioSummary)
                .nomeEmpresa(organizador.getNomeEmpresa())
                .cnpj(organizador.getCnpj())
                .telefone(organizador.getTelefone())
                .endereco(organizador.getEndereco())
                .descricao(organizador.getDescricao())
                .site(organizador.getSite())
                .verificado(organizador.getVerificado())
                .createdAt(organizador.getCreatedAt())
                .updatedAt(organizador.getUpdatedAt())
                .nomeExibicao(organizador.getNomeExibicao())
                .totalEventos(organizador.getTotalEventos())
                .podeOrganizarEventos(organizador.podeOrganizarEventos())
                .build();
    }

    // DTO auxiliar para estatísticas
    public static class UsuarioEstatisticasDTO {
        private Long totalUsuarios;
        private Long totalAtletas;
        private Long totalOrganizadores;
        private Long usuariosAtivosUltimos30Dias;

        public static UsuarioEstatisticasDTOBuilder builder() {
            return new UsuarioEstatisticasDTOBuilder();
        }

        public static class UsuarioEstatisticasDTOBuilder {
            private Long totalUsuarios;
            private Long totalAtletas;
            private Long totalOrganizadores;
            private Long usuariosAtivosUltimos30Dias;

            public UsuarioEstatisticasDTOBuilder totalUsuarios(Long totalUsuarios) {
                this.totalUsuarios = totalUsuarios;
                return this;
            }

            public UsuarioEstatisticasDTOBuilder totalAtletas(Long totalAtletas) {
                this.totalAtletas = totalAtletas;
                return this;
            }

            public UsuarioEstatisticasDTOBuilder totalOrganizadores(Long totalOrganizadores) {
                this.totalOrganizadores = totalOrganizadores;
                return this;
            }

            public UsuarioEstatisticasDTOBuilder usuariosAtivosUltimos30Dias(Long usuariosAtivosUltimos30Dias) {
                this.usuariosAtivosUltimos30Dias = usuariosAtivosUltimos30Dias;
                return this;
            }

            public UsuarioEstatisticasDTO build() {
                UsuarioEstatisticasDTO dto = new UsuarioEstatisticasDTO();
                dto.totalUsuarios = this.totalUsuarios;
                dto.totalAtletas = this.totalAtletas;
                dto.totalOrganizadores = this.totalOrganizadores;
                dto.usuariosAtivosUltimos30Dias = this.usuariosAtivosUltimos30Dias;
                return dto;
            }
        }

        // Getters
        public Long getTotalUsuarios() { return totalUsuarios; }
        public Long getTotalAtletas() { return totalAtletas; }
        public Long getTotalOrganizadores() { return totalOrganizadores; }
        public Long getUsuariosAtivosUltimos30Dias() { return usuariosAtivosUltimos30Dias; }
    }
}
