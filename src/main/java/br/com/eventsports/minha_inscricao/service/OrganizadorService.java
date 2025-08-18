package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.organizador.*;
import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioSummaryDTO;
import br.com.eventsports.minha_inscricao.entity.OrganizadorEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.repository.OrganizadorRepository;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IOrganizadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrganizadorService implements IOrganizadorService {

    private final OrganizadorRepository organizadorRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Cria um novo organizador
     */
    @CacheEvict(value = "organizadores", allEntries = true)
    public OrganizadorResponseDTO criar(OrganizadorCreateDTO dto) {
        log.info("Criando novo organizador para usuário ID: {}", dto.getUsuarioId());
        
        // Validar se usuário existe e é do tipo ORGANIZADOR
        UsuarioEntity usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        if (!TipoUsuario.ORGANIZADOR.equals(usuario.getTipo())) {
            throw new IllegalArgumentException("Usuário deve ser do tipo ORGANIZADOR");
        }
        
        if (!usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário deve estar ativo");
        }

        // Validar se já existe organizador para este usuário
        if (organizadorRepository.existsByUsuarioId(dto.getUsuarioId())) {
            throw new IllegalArgumentException("Já existe organizador para este usuário");
        }

        // Validar CNPJ se informado
        if (dto.getCnpj() != null && organizadorRepository.existsByCnpj(dto.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já está em uso: " + dto.getCnpj());
        }

        // Criar entidade
        OrganizadorEntity organizador = OrganizadorEntity.builder()
                .usuario(usuario)
                .nomeEmpresa(dto.getNomeEmpresa())
                .cnpj(dto.getCnpj())
                .telefone(dto.getTelefone())
                .endereco(dto.getEndereco())
                .descricao(dto.getDescricao())
                .site(dto.getSite())
                .verificado(false)
                .build();

        // Salvar
        OrganizadorEntity organizadorSalvo = organizadorRepository.save(organizador);
        
        log.info("Organizador criado com sucesso. ID: {}", organizadorSalvo.getId());
        return mapToResponseDTO(organizadorSalvo);
    }

    /**
     * Busca organizador por ID
     */
    @Cacheable(value = "organizadores", key = "#id")
    @Transactional(readOnly = true)
    public OrganizadorResponseDTO buscarPorId(Long id) {
        log.debug("Buscando organizador por ID: {}", id);
        
        OrganizadorEntity organizador = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + id));
        
        return mapToResponseDTO(organizador);
    }

    /**
     * Busca organizador por ID do usuário
     */
    @Transactional(readOnly = true)
    public OrganizadorResponseDTO buscarPorUsuarioId(Long usuarioId) {
        log.debug("Buscando organizador por usuário ID: {}", usuarioId);
        
        OrganizadorEntity organizador = organizadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado para usuário ID: " + usuarioId));
        
        return mapToResponseDTO(organizador);
    }

    /**
     * Busca organizador por CNPJ
     */
    @Transactional(readOnly = true)
    public OrganizadorResponseDTO buscarPorCnpj(String cnpj) {
        log.debug("Buscando organizador por CNPJ: {}", cnpj);
        
        OrganizadorEntity organizador = organizadorRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com CNPJ: " + cnpj));
        
        return mapToResponseDTO(organizador);
    }

    /**
     * Lista todos os organizadores
     */
    @Transactional(readOnly = true)
    public Page<OrganizadorSummaryDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos os organizadores com paginação");
        
        return organizadorRepository.findAll(pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Lista organizadores verificados
     */
    @Transactional(readOnly = true)
    public Page<OrganizadorSummaryDTO> listarVerificados(Pageable pageable) {
        log.debug("Listando organizadores verificados com paginação");
        
        return organizadorRepository.findByVerificadoTrue(pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Lista organizadores que podem organizar eventos
     */
    @Transactional(readOnly = true)
    public Page<OrganizadorSummaryDTO> listarQuePodemOrganizarEventos(Pageable pageable) {
        log.debug("Listando organizadores que podem organizar eventos");
        
        return organizadorRepository.findQuePodemOrganizarEventos(pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Busca organizadores por nome da empresa
     */
    @Transactional(readOnly = true)
    public Page<OrganizadorSummaryDTO> buscarPorNomeEmpresa(String nomeEmpresa, Pageable pageable) {
        log.debug("Buscando organizadores por nome da empresa: {}", nomeEmpresa);
        
        return organizadorRepository.findByNomeEmpresaContainingIgnoreCase(nomeEmpresa, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Busca organizadores verificados por nome da empresa
     */
    @Transactional(readOnly = true)
    public Page<OrganizadorSummaryDTO> buscarVerificadosPorNomeEmpresa(String nomeEmpresa, Pageable pageable) {
        log.debug("Buscando organizadores verificados por nome da empresa: {}", nomeEmpresa);
        
        return organizadorRepository.findByNomeEmpresaContainingIgnoreCaseAndVerificadoTrue(nomeEmpresa, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Atualiza dados do organizador
     */
    @CacheEvict(value = "organizadores", key = "#id")
    public OrganizadorResponseDTO atualizar(Long id, OrganizadorUpdateDTO dto) {
        log.info("Atualizando organizador ID: {}", id);
        
        OrganizadorEntity organizador = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + id));

        // Validar CNPJ se foi alterado
        if (dto.getCnpj() != null && !dto.getCnpj().equals(organizador.getCnpj())) {
            if (organizadorRepository.existsByCnpj(dto.getCnpj())) {
                throw new IllegalArgumentException("CNPJ já está em uso: " + dto.getCnpj());
            }
            organizador.setCnpj(dto.getCnpj());
        }

        // Atualizar campos se informados
        if (dto.getNomeEmpresa() != null) {
            organizador.setNomeEmpresa(dto.getNomeEmpresa());
        }
        
        if (dto.getTelefone() != null) {
            organizador.setTelefone(dto.getTelefone());
        }
        
        if (dto.getEndereco() != null) {
            organizador.setEndereco(dto.getEndereco());
        }
        
        if (dto.getDescricao() != null) {
            organizador.setDescricao(dto.getDescricao());
        }
        
        if (dto.getSite() != null) {
            organizador.setSite(dto.getSite());
        }
        
        if (dto.getVerificado() != null) {
            organizador.setVerificado(dto.getVerificado());
        }

        OrganizadorEntity organizadorAtualizado = organizadorRepository.save(organizador);
        
        log.info("Organizador atualizado com sucesso. ID: {}", id);
        return mapToResponseDTO(organizadorAtualizado);
    }

    /**
     * Verifica um organizador
     */
    @CacheEvict(value = "organizadores", key = "#id")
    public void verificar(Long id) {
        log.info("Verificando organizador ID: {}", id);
        
        OrganizadorEntity organizador = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + id));
        
        organizador.verificar();
        organizadorRepository.save(organizador);
        
        log.info("Organizador verificado com sucesso. ID: {}", id);
    }

    /**
     * Remove verificação de um organizador
     */
    @CacheEvict(value = "organizadores", key = "#id")
    public void removerVerificacao(Long id) {
        log.info("Removendo verificação do organizador ID: {}", id);
        
        OrganizadorEntity organizador = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + id));
        
        organizador.removerVerificacao();
        organizadorRepository.save(organizador);
        
        log.info("Verificação removida com sucesso. ID: {}", id);
    }

    /**
     * Lista organizadores por cidade
     */
    @Transactional(readOnly = true)
    public List<OrganizadorSummaryDTO> listarPorCidade(String cidade) {
        log.debug("Listando organizadores por cidade: {}", cidade);
        
        return organizadorRepository.findByCidade(cidade).stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista organizadores com eventos
     */
    @Transactional(readOnly = true)
    public List<OrganizadorSummaryDTO> listarComEventos() {
        log.debug("Listando organizadores com eventos");
        
        return organizadorRepository.findComEventos().stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtém estatísticas de organizadores
     */
    @Transactional(readOnly = true)
    public OrganizadorEstatisticasDTO obterEstatisticas() {
        log.debug("Obtendo estatísticas de organizadores");
        
        Long totalOrganizadores = organizadorRepository.count();
        Long organizadoresVerificados = organizadorRepository.countVerificados();
        List<OrganizadorEntity> comEventos = organizadorRepository.findComEventos();
        List<OrganizadorEntity> quePodemOrganizar = organizadorRepository.findQuePodemOrganizarEventos();
        
        return OrganizadorEstatisticasDTO.builder()
                .totalOrganizadores(totalOrganizadores)
                .organizadoresVerificados(organizadoresVerificados)
                .organizadoresComEventos((long) comEventos.size())
                .organizadoresQuePodemOrganizar((long) quePodemOrganizar.size())
                .build();
    }

    /**
     * Verifica se organizador pode organizar eventos
     */
    @Transactional(readOnly = true)
    public boolean podeOrganizarEventos(Long id) {
        log.debug("Verificando se organizador ID {} pode organizar eventos", id);
        
        OrganizadorEntity organizador = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador não encontrado com ID: " + id));
        
        return organizador.podeOrganizarEventos();
    }

    // Métodos auxiliares de mapeamento
    private OrganizadorResponseDTO mapToResponseDTO(OrganizadorEntity organizador) {
        return OrganizadorResponseDTO.builder()
                .id(organizador.getId())
                .usuario(mapUsuarioToSummaryDTO(organizador.getUsuario()))
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

    private OrganizadorSummaryDTO mapToSummaryDTO(OrganizadorEntity organizador) {
        return OrganizadorSummaryDTO.builder()
                .id(organizador.getId())
                .nomeEmpresa(organizador.getNomeEmpresa())
                .nomeExibicao(organizador.getNomeExibicao())
                .verificado(organizador.getVerificado())
                .totalEventos(organizador.getTotalEventos())
                .podeOrganizarEventos(organizador.podeOrganizarEventos())
                .build();
    }

    private UsuarioSummaryDTO mapUsuarioToSummaryDTO(UsuarioEntity usuario) {
        return UsuarioSummaryDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .tipo(usuario.getTipo())
                .ativo(usuario.getAtivo())
                .build();
    }

    // DTO auxiliar para estatísticas
    public static class OrganizadorEstatisticasDTO {
        private Long totalOrganizadores;
        private Long organizadoresVerificados;
        private Long organizadoresComEventos;
        private Long organizadoresQuePodemOrganizar;

        public static OrganizadorEstatisticasDTOBuilder builder() {
            return new OrganizadorEstatisticasDTOBuilder();
        }

        public static class OrganizadorEstatisticasDTOBuilder {
            private Long totalOrganizadores;
            private Long organizadoresVerificados;
            private Long organizadoresComEventos;
            private Long organizadoresQuePodemOrganizar;

            public OrganizadorEstatisticasDTOBuilder totalOrganizadores(Long totalOrganizadores) {
                this.totalOrganizadores = totalOrganizadores;
                return this;
            }

            public OrganizadorEstatisticasDTOBuilder organizadoresVerificados(Long organizadoresVerificados) {
                this.organizadoresVerificados = organizadoresVerificados;
                return this;
            }

            public OrganizadorEstatisticasDTOBuilder organizadoresComEventos(Long organizadoresComEventos) {
                this.organizadoresComEventos = organizadoresComEventos;
                return this;
            }

            public OrganizadorEstatisticasDTOBuilder organizadoresQuePodemOrganizar(Long organizadoresQuePodemOrganizar) {
                this.organizadoresQuePodemOrganizar = organizadoresQuePodemOrganizar;
                return this;
            }

            public OrganizadorEstatisticasDTO build() {
                OrganizadorEstatisticasDTO dto = new OrganizadorEstatisticasDTO();
                dto.totalOrganizadores = this.totalOrganizadores;
                dto.organizadoresVerificados = this.organizadoresVerificados;
                dto.organizadoresComEventos = this.organizadoresComEventos;
                dto.organizadoresQuePodemOrganizar = this.organizadoresQuePodemOrganizar;
                return dto;
            }
        }

        // Getters
        public Long getTotalOrganizadores() { return totalOrganizadores; }
        public Long getOrganizadoresVerificados() { return organizadoresVerificados; }
        public Long getOrganizadoresComEventos() { return organizadoresComEventos; }
        public Long getOrganizadoresQuePodemOrganizar() { return organizadoresQuePodemOrganizar; }
    }
}
