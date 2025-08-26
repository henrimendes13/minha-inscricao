package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.atleta.*;
import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.InscricaoEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import br.com.eventsports.minha_inscricao.repository.AtletaRepository;
import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.EquipeRepository;
import br.com.eventsports.minha_inscricao.repository.InscricaoRepository;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAtletaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AtletaService implements IAtletaService {

    private final AtletaRepository atletaRepository;
    private final EventoRepository eventoRepository;
    private final EquipeRepository equipeRepository;
    private final CategoriaRepository categoriaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Cacheable(value = "atletas", key = "#id")
    @Transactional(readOnly = true)
    public AtletaResponseDTO findById(Long id) {
        AtletaEntity atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado com ID: " + id));
        return convertToResponseDTO(atleta);
    }

    @Transactional(readOnly = true)
    public Optional<AtletaEntity> findEntityById(Long id) {
        return atletaRepository.findById(id);
    }

    @Cacheable(value = "atletas", key = "'all'")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findAll() {
        List<AtletaEntity> atletas = atletaRepository.findAll();
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "atletas", key = "#result.id")
    @CacheEvict(value = "atletas", key = "'all'")
    public AtletaResponseDTO save(AtletaCreateDTO atletaCreateDTO) {
        validateAtletaData(atletaCreateDTO);
        AtletaEntity atleta = convertCreateDTOToEntity(atletaCreateDTO);
        AtletaEntity savedAtleta = atletaRepository.save(atleta);
        return convertToResponseDTO(savedAtleta);
    }

    @CachePut(value = "atletas", key = "#result.id")
    @CacheEvict(value = "atletas", key = "'all'")
    public AtletaResponseDTO saveForInscricao(AtletaCreateDTO atletaCreateDTO, Long eventoId, Long equipeId) {
        validateAtletaData(atletaCreateDTO);
        AtletaEntity atleta = convertCreateDTOToEntityForInscricao(atletaCreateDTO, eventoId, equipeId);
        AtletaEntity savedAtleta = atletaRepository.save(atleta);
        return convertToResponseDTO(savedAtleta);
    }

    /**
     * Cria um atleta individual com inscrição automática no evento.
     * @param eventoId ID do evento
     * @param atletaInscricaoDTO Dados do atleta e da inscrição
     * @return AtletaResponseDTO com atleta criado e inscrição associada
     */
    @CachePut(value = "atletas", key = "#result.id")
    @CacheEvict(value = "atletas", key = "'all'")
    @Transactional
    public AtletaResponseDTO criarAtletaParaInscricao(Long eventoId, AtletaInscricaoDTO atletaInscricaoDTO) {
        validateAtletaInscricaoData(eventoId, atletaInscricaoDTO);
        
        // Buscar entidades necessárias
        EventoEntity evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        CategoriaEntity categoria = categoriaRepository.findById(atletaInscricaoDTO.getCategoriaId())
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        // Verificar se já existe atleta com mesmo CPF
        AtletaEntity atleta = null;
        if (atletaInscricaoDTO.getCpf() != null) {
            Optional<AtletaEntity> atletaExistente = atletaRepository.findByCpf(atletaInscricaoDTO.getCpf());
            if (atletaExistente.isPresent()) {
                atleta = atletaExistente.get();
                // Verifica se já tem inscrição no mesmo evento
                if (temInscricaoNoEvento(atleta, eventoId)) {
                    throw new RuntimeException("Atleta já possui inscrição neste evento");
                }
            }
        }

        // Se não existe atleta, criar novo
        if (atleta == null) {
            atleta = convertInscricaoDTOToEntity(atletaInscricaoDTO, evento);
            atleta = atletaRepository.save(atleta);
        }

        // Criar UsuarioEntity baseado no atleta para a inscrição
        UsuarioEntity usuario = criarUsuarioParaInscricao(atleta, atletaInscricaoDTO);
        usuario = usuarioRepository.save(usuario);

        // Criar inscrição individual
        InscricaoEntity inscricao = criarInscricaoParaAtleta(usuario, evento, categoria, atletaInscricaoDTO);
        
        // Recarregar atleta para obter dados atualizados
        atleta = atletaRepository.findById(atleta.getId()).orElse(atleta);

        return convertToResponseDTO(atleta);
    }

    @CachePut(value = "atletas", key = "#id")
    @CacheEvict(value = "atletas", key = "'all'")
    public AtletaResponseDTO update(Long id, AtletaUpdateDTO atletaUpdateDTO) {
        AtletaEntity atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado com ID: " + id));
        
        updateAtletaFromDTO(atleta, atletaUpdateDTO);
        AtletaEntity updatedAtleta = atletaRepository.save(atleta);
        return convertToResponseDTO(updatedAtleta);
    }

    @CacheEvict(value = "atletas", allEntries = true)
    public void deleteById(Long id) {
        if (!atletaRepository.existsById(id)) {
            throw new RuntimeException("Atleta não encontrado com ID: " + id);
        }
        atletaRepository.deleteById(id);
    }

    @Cacheable(value = "atletas", key = "'byCpf:' + #cpf")
    @Transactional(readOnly = true)
    public Optional<AtletaResponseDTO> findByCpf(String cpf) {
        return atletaRepository.findByCpf(cpf)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AtletaEntity> findEntityByCpf(String cpf) {
        return atletaRepository.findByCpf(cpf);
    }

    @Cacheable(value = "atletas", key = "'byNome:' + #nome")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findByNome(String nome) {
        List<AtletaEntity> atletas = atletaRepository.findByNomeContainingIgnoreCase(nome);
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "atletas", key = "'byGenero:' + #genero")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findByGenero(Genero genero) {
        List<AtletaEntity> atletas = atletaRepository.findByGenero(genero);
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "atletas", key = "'byEvento:' + #eventoId")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findByEventoId(Long eventoId) {
        List<AtletaEntity> atletas = atletaRepository.findByEventoId(eventoId);
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "atletas", key = "'byEquipe:' + #equipeId")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findByEquipeId(Long equipeId) {
        List<AtletaEntity> atletas = atletaRepository.findByEquipeId(equipeId);
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "atletas", key = "'atletasAtivos'")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findAtletasAtivos() {
        List<AtletaEntity> atletas = atletaRepository.findAtletasAtivos();
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "atletas", key = "'atletasComContatoEmergencia'")
    @Transactional(readOnly = true)
    public List<AtletaSummaryDTO> findAtletasComContatoEmergencia() {
        List<AtletaEntity> atletas = atletaRepository.findAtletasComContatoEmergencia();
        return atletas.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "atletas", allEntries = true)
    public void updateAceitaTermos(Long id, Boolean aceitaTermos) {
        atletaRepository.updateAceitaTermos(id, aceitaTermos);
    }

    @Transactional(readOnly = true)
    public boolean existsByCpf(String cpf) {
        return atletaRepository.existsByCpf(cpf);
    }

    @Transactional(readOnly = true)
    public long countAtletasAtivosByEventoId(Long eventoId) {
        return atletaRepository.countAtletasAtivosByEventoId(eventoId);
    }

    @Transactional(readOnly = true)
    public long countAtletasByEquipeId(Long equipeId) {
        return atletaRepository.countAtletasByEquipeId(equipeId);
    }

    // Métodos de conversão
    private AtletaResponseDTO convertToResponseDTO(AtletaEntity atleta) {
        return AtletaResponseDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNome())
                .cpf(atleta.getCpf())
                .dataNascimento(atleta.getDataNascimento())
                .genero(atleta.getGenero())
                .telefone(atleta.getTelefone())
                .emergenciaNome(atleta.getEmergenciaNome())
                .emergenciaTelefone(atleta.getEmergenciaTelefone())
                .observacoesMedicas(atleta.getObservacoesMedicas())
                .endereco(atleta.getEndereco())
                .aceitaTermos(atleta.getAceitaTermos())
                .createdAt(atleta.getCreatedAt())
                .updatedAt(atleta.getUpdatedAt())
                .idade(atleta.getIdade())
                .maiorIdade(atleta.isMaiorIdade())
                .temContatoEmergencia(atleta.temContatoEmergencia())
                .podeParticipar(atleta.podeParticipar())
                .eventoId(atleta.getEvento() != null ? atleta.getEvento().getId() : null)
                .nomeEvento(atleta.getNomeEvento())
                .inscricaoId(atleta.getInscricaoId())
                .statusInscricao(atleta.getStatusInscricao())
                .nomeCategoriaInscricao(atleta.getNomeCategoriaInscricao())
                .equipeId(atleta.getEquipeId())
                .nomeEquipe(atleta.getNomeEquipe())
                .build();
    }

    private AtletaSummaryDTO convertToSummaryDTO(AtletaEntity atleta) {
        return AtletaSummaryDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNome())
                .dataNascimento(atleta.getDataNascimento())
                .genero(atleta.getGenero())
                .telefone(atleta.getTelefone())
                .aceitaTermos(atleta.getAceitaTermos())
                .idade(atleta.getIdade())
                .podeParticipar(atleta.podeParticipar())
                .nomeEvento(atleta.getNomeEvento())
                .statusInscricao(atleta.getStatusInscricao())
                .nomeEquipe(atleta.getNomeEquipe())
                .build();
    }

    private AtletaEntity convertCreateDTOToEntity(AtletaCreateDTO dto) {
        return AtletaEntity.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .dataNascimento(dto.getDataNascimento())
                .genero(dto.getGenero())
                .telefone(dto.getTelefone())
                .emergenciaNome(dto.getEmergenciaNome())
                .emergenciaTelefone(dto.getEmergenciaTelefone())
                .observacoesMedicas(dto.getObservacoesMedicas())
                .endereco(dto.getEndereco())
                .aceitaTermos(dto.getAceitaTermos())
                .build();
    }

    private AtletaEntity convertCreateDTOToEntityForInscricao(AtletaCreateDTO dto, Long eventoId, Long equipeId) {
        AtletaEntity atleta = convertCreateDTOToEntity(dto);

        // Definir evento obrigatório
        if (eventoId != null) {
            EventoEntity evento = eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));
            atleta.setEvento(evento);
        }

        // Definir equipe se fornecida
        if (equipeId != null) {
            EquipeEntity equipe = equipeRepository.findById(equipeId)
                    .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + equipeId));
            atleta.setEquipe(equipe);
        }

        return atleta;
    }

    private AtletaEntity convertCreateDTOToEntityForEvento(AtletaCreateDTO dto, Long eventoId) {
        AtletaEntity atleta = convertCreateDTOToEntity(dto);

        // Definir evento obrigatório
        if (eventoId != null) {
            EventoEntity evento = eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));
            atleta.setEvento(evento);
        }

        return atleta;
    }

    private void updateAtletaFromDTO(AtletaEntity atleta, AtletaUpdateDTO dto) {
        if (dto.getNome() != null) {
            atleta.setNome(dto.getNome());
        }
        if (dto.getCpf() != null) {
            atleta.setCpf(dto.getCpf());
        }
        if (dto.getDataNascimento() != null) {
            atleta.setDataNascimento(dto.getDataNascimento());
        }
        if (dto.getGenero() != null) {
            atleta.setGenero(dto.getGenero());
        }
        if (dto.getTelefone() != null) {
            atleta.setTelefone(dto.getTelefone());
        }
        if (dto.getEmergenciaNome() != null) {
            atleta.setEmergenciaNome(dto.getEmergenciaNome());
        }
        if (dto.getEmergenciaTelefone() != null) {
            atleta.setEmergenciaTelefone(dto.getEmergenciaTelefone());
        }
        if (dto.getObservacoesMedicas() != null) {
            atleta.setObservacoesMedicas(dto.getObservacoesMedicas());
        }
        if (dto.getEndereco() != null) {
            atleta.setEndereco(dto.getEndereco());
        }
        if (dto.getAceitaTermos() != null) {
            atleta.setAceitaTermos(dto.getAceitaTermos());
        }
    }

    private void validateAtletaData(AtletaCreateDTO dto) {
        // Validar CPF único se fornecido
        if (dto.getCpf() != null && atletaRepository.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("Já existe um atleta cadastrado com o CPF: " + dto.getCpf());
        }

        // Validar idade mínima
        if (dto.getDataNascimento() != null) {
            LocalDate hoje = LocalDate.now();
            int idade = hoje.getYear() - dto.getDataNascimento().getYear();
            if (dto.getDataNascimento().isAfter(hoje.minusYears(idade))) {
                idade--;
            }
            
            if (idade < 0) {
                throw new RuntimeException("Data de nascimento não pode ser no futuro");
            }
        }

        // Validar aceite dos termos
        if (dto.getAceitaTermos() == null || !dto.getAceitaTermos()) {
            throw new RuntimeException("É obrigatório aceitar os termos para cadastrar o atleta");
        }
    }

    /**
     * Valida os dados de criação de atleta para inscrição.
     */
    private void validateAtletaInscricaoData(Long eventoId, AtletaInscricaoDTO dto) {
        // Validações básicas do atleta
        if (dto.getDataNascimento() != null) {
            LocalDate hoje = LocalDate.now();
            int idade = hoje.getYear() - dto.getDataNascimento().getYear();
            if (dto.getDataNascimento().isAfter(hoje.minusYears(idade))) {
                idade--;
            }
            
            if (idade < 0) {
                throw new RuntimeException("Data de nascimento não pode ser no futuro");
            }
        }

        // Validar aceite dos termos
        if (dto.getAceitaTermos() == null || !dto.getAceitaTermos()) {
            throw new RuntimeException("É obrigatório aceitar os termos para cadastrar o atleta");
        }

        if (dto.getTermosInscricaoAceitos() == null || !dto.getTermosInscricaoAceitos()) {
            throw new RuntimeException("É obrigatório aceitar os termos da inscrição");
        }

        // Validar categoria
        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getAtiva()) {
            throw new RuntimeException("Categoria não está ativa para inscrições");
        }

        if (!TipoParticipacao.INDIVIDUAL.equals(categoria.getTipoParticipacao())) {
            throw new RuntimeException("Esta categoria não aceita inscrições individuais");
        }

        // Validar compatibilidade do atleta com a categoria
        if (!atletaEhCompativel(dto, categoria)) {
            throw new RuntimeException("Atleta não atende aos critérios da categoria (idade/gênero)");
        }
    }

    /**
     * Verifica se um atleta é compatível com uma categoria.
     */
    private boolean atletaEhCompativel(AtletaInscricaoDTO dto, CategoriaEntity categoria) {
        // Verificar gênero
        if (categoria.getGenero() != null && !categoria.getGenero().equals(dto.getGenero())) {
            return false;
        }

        // Calcular idade baseada na data de nascimento
        int idade = java.time.Period.between(dto.getDataNascimento(), LocalDate.now()).getYears();

        // Verificar idade mínima
        if (categoria.getIdadeMinima() != null && idade < categoria.getIdadeMinima()) {
            return false;
        }

        // Verificar idade máxima
        if (categoria.getIdadeMaxima() != null && idade > categoria.getIdadeMaxima()) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se o atleta já tem inscrição no evento.
     */
    private boolean temInscricaoNoEvento(AtletaEntity atleta, Long eventoId) {
        // Implementar consulta para verificar se existe inscrição
        return inscricaoRepository.existsByAtletaIdAndEventoId(atleta.getId(), eventoId);
    }

    /**
     * Converte DTO de inscrição para AtletaEntity.
     */
    private AtletaEntity convertInscricaoDTOToEntity(AtletaInscricaoDTO dto, EventoEntity evento) {
        return AtletaEntity.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .dataNascimento(dto.getDataNascimento())
                .genero(dto.getGenero())
                .telefone(dto.getTelefone())
                .emergenciaNome(dto.getEmergenciaNome())
                .emergenciaTelefone(dto.getEmergenciaTelefone())
                .observacoesMedicas(dto.getObservacoesMedicas())
                .endereco(dto.getEndereco())
                .email(dto.getEmail())
                .aceitaTermos(dto.getAceitaTermos())
                .evento(evento)
                .build();
    }

    /**
     * Cria UsuarioEntity baseado no atleta para a inscrição.
     */
    private UsuarioEntity criarUsuarioParaInscricao(AtletaEntity atleta, AtletaInscricaoDTO dto) {
        // Verificar se já existe usuário com mesmo CPF
        if (dto.getCpf() != null) {
            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findByCpf(dto.getCpf());
            if (usuarioExistente.isPresent()) {
                return usuarioExistente.get();
            }
        }

        // Criar novo usuário baseado no atleta
        return UsuarioEntity.builder()
                .nome(atleta.getNome())
                .cpf(atleta.getCpf())
                .dataNascimento(atleta.getDataNascimento())
                .genero(atleta.getGenero())
                .telefone(atleta.getTelefone())
                .email(atleta.getEmail())
                .aceitaTermos(dto.getTermosInscricaoAceitos())
                .ativo(true)
                .senha("temp123") // Senha temporária - deve ser alterada pelo usuário
                .build();
    }

    /**
     * Cria uma inscrição para o atleta individual.
     */
    private InscricaoEntity criarInscricaoParaAtleta(UsuarioEntity usuario, EventoEntity evento, 
                                                    CategoriaEntity categoria, AtletaInscricaoDTO dto) {
        // Determinar valor da inscrição
        BigDecimal valorInscricao = dto.getValorInscricao() != null 
            ? dto.getValorInscricao()
            : (categoria.getValorInscricao() != null 
                ? categoria.getValorInscricao() 
                : BigDecimal.ZERO);

        // Criar a inscrição individual
        InscricaoEntity inscricao = InscricaoEntity.builder()
                .atleta(usuario) // Inscrição individual
                .evento(evento)
                .categoria(categoria)
                .equipe(null) // Não é inscrição de equipe
                .status(StatusInscricao.PENDENTE)
                .valor(valorInscricao)
                .dataInscricao(LocalDateTime.now())
                .termosAceitos(dto.getTermosInscricaoAceitos())
                .codigoDesconto(dto.getCodigoDesconto())
                .build();

        // Salvar inscrição
        return inscricaoRepository.save(inscricao);
    }

    /**
     * Método temporário para compatibilidade com EquipeService.
     * Cria atleta apenas para evento, sem inscrição.
     */
    public AtletaResponseDTO saveForEvento(AtletaCreateDTO atletaCreateDTO, Long eventoId) {
        validateAtletaData(atletaCreateDTO);
        AtletaEntity atleta = convertCreateDTOToEntityForEvento(atletaCreateDTO, eventoId);
        AtletaEntity savedAtleta = atletaRepository.save(atleta);
        return convertToResponseDTO(savedAtleta);
    }
}
