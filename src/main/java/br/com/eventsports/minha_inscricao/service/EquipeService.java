package br.com.eventsports.minha_inscricao.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaCreateDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeCreateDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeInscricaoDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeResponseDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeUpdateDTO;
import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.InscricaoEntity;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import br.com.eventsports.minha_inscricao.repository.AtletaRepository;
import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import br.com.eventsports.minha_inscricao.repository.EquipeRepository;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.InscricaoRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAtletaService;
import br.com.eventsports.minha_inscricao.service.Interfaces.IEquipeService;
import br.com.eventsports.minha_inscricao.service.Interfaces.IUsuarioService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EquipeService implements IEquipeService {

    private final EquipeRepository equipeRepository;
    private final CategoriaRepository categoriaRepository;
    private final AtletaRepository atletaRepository;
    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final IAtletaService atletaService;
    private final IUsuarioService usuarioService;

    @Cacheable(value = "equipes", key = "#id")
    @Transactional(readOnly = true)
    public EquipeResponseDTO findById(Long id) {
        EquipeEntity equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + id));
        return convertToResponseDTO(equipe);
    }

    @Cacheable(value = "equipes", key = "'all'")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findAll() {
        List<EquipeEntity> equipes = equipeRepository.findAll();
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "equipes", key = "#result.id")
    @CacheEvict(value = "equipes", key = "'all'")
    public EquipeResponseDTO save(EquipeCreateDTO equipeCreateDTO) {
        validateEquipeData(equipeCreateDTO);
        EquipeEntity equipe = convertCreateDTOToEntity(equipeCreateDTO);
        EquipeEntity savedEquipe = equipeRepository.save(equipe);
        return convertToResponseDTO(savedEquipe);
    }

    /**
     * Cria uma equipe no contexto de inscrição em um evento específico.
     * Este método é usado durante o processo de inscrição.
     */
    public EquipeResponseDTO criarEquipeParaInscricao(Long eventoId, EquipeInscricaoDTO equipeInscricaoDTO) {
        return criarEquipeParaInscricao(eventoId, equipeInscricaoDTO, null);
    }

    /**
     * Cria uma equipe no contexto de inscrição em um evento específico.
     * 
     * @param eventoId           ID do evento
     * @param equipeInscricaoDTO Dados da equipe
     * @param usuarioLogadoId    ID do usuário logado (null para usar o primeiro da
     *                           lista)
     */
    @Transactional
    public EquipeResponseDTO criarEquipeParaInscricao(Long eventoId, EquipeInscricaoDTO equipeInscricaoDTO,
            Long usuarioLogadoId) {
        validateEquipeInscricaoData(eventoId, equipeInscricaoDTO);

        // Buscar entidades necessárias
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        CategoriaEntity categoria = categoriaRepository.findById(equipeInscricaoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        // Criar equipe básica (sem capitão ainda)
        EquipeEntity equipe = new EquipeEntity();
        equipe.setNome(equipeInscricaoDTO.getNome());
        equipe.setEvento(evento);
        equipe.setCategoria(categoria);
        equipe.setDescricao(null);
        equipe.setAtiva(true);

        // Criar todos os atletas primeiro
        List<AtletaEntity> atletasCriados = new ArrayList<>();
        for (AtletaCreateDTO atletaDto : equipeInscricaoDTO.getAtletas()) {
            // Verificar se já existe
            Optional<AtletaEntity> atletaExistente = atletaRepository.findByCpf(atletaDto.getCpf());
            if (atletaExistente.isPresent()) {
                AtletaEntity atleta = atletaExistente.get();
                // Se atleta já existe mas não tem categoria, definir agora
                if (atleta.getCategoria() == null) {
                    atleta.setCategoria(categoria);
                    atleta = atletaRepository.save(atleta);
                }
                atletasCriados.add(atleta);
            } else {
                // Criar novo atleta
                AtletaEntity novoAtleta = new AtletaEntity();
                novoAtleta.setNome(atletaDto.getNome());
                novoAtleta.setCpf(atletaDto.getCpf());
                novoAtleta.setDataNascimento(atletaDto.getDataNascimento());
                novoAtleta.setGenero(atletaDto.getGenero());
                novoAtleta.setTelefone(atletaDto.getTelefone());
                novoAtleta.setEmergenciaNome(atletaDto.getEmergenciaNome());
                novoAtleta.setEmergenciaTelefone(atletaDto.getEmergenciaTelefone());
                novoAtleta.setObservacoesMedicas(atletaDto.getObservacoesMedicas());
                novoAtleta.setEndereco(atletaDto.getEndereco());
                novoAtleta.setEmail(atletaDto.getEmail());
                novoAtleta.setAceitaTermos(atletaDto.getAceitaTermos());
                novoAtleta.setEvento(evento);
                novoAtleta.setCategoria(categoria);
                novoAtleta = atletaRepository.save(novoAtleta);
                atletasCriados.add(novoAtleta);
            }
        }

        // Definir capitão
        AtletaEntity capitao = atletasCriados.get(0); // Default: primeiro atleta
        if (equipeInscricaoDTO.getCapitaoCpf() != null) {
            for (AtletaEntity atleta : atletasCriados) {
                if (equipeInscricaoDTO.getCapitaoCpf().equals(atleta.getCpf())) {
                    capitao = atleta;
                    break;
                }
            }
        }

        equipe.setCapitao(capitao);

        // Salvar a equipe
        equipe = equipeRepository.save(equipe);

        // Associar atletas à equipe
        for (AtletaEntity atleta : atletasCriados) {
            atleta.setEquipe(equipe);
            atletaRepository.save(atleta);
        }

        // Criar inscrição para a equipe
        InscricaoEntity inscricao = criarInscricaoParaEquipe(equipe, equipeInscricaoDTO);

        // Recarregar a equipe com a inscrição associada
        equipe = equipeRepository.findById(equipe.getId()).orElse(equipe);

        // Retornar resposta completa com inscrição
        return convertToResponseDTO(equipe);
    }

    @CachePut(value = "equipes", key = "#id")
    @CacheEvict(value = "equipes", key = "'all'")
    public EquipeResponseDTO update(Long id, EquipeUpdateDTO equipeUpdateDTO) {
        validateEquipeUpdateData(id, equipeUpdateDTO);
        EquipeEntity existingEquipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + id));

        updateEquipeFromUpdateDTO(existingEquipe, equipeUpdateDTO);
        EquipeEntity updatedEquipe = equipeRepository.save(existingEquipe);
        return convertToResponseDTO(updatedEquipe);
    }

    @CacheEvict(value = "equipes", allEntries = true)
    public void deleteById(Long id) {
        EquipeEntity equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + id));

        equipe.desativar();
        equipeRepository.save(equipe);
    }

    @Cacheable(value = "equipes", key = "'search:' + #nome")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findByNome(String nome) {
        List<EquipeEntity> equipes = equipeRepository.findByNomeContainingIgnoreCase(nome);
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "equipes", key = "'byEvento:' + #eventoId")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findByEventoId(Long eventoId) {
        List<EquipeEntity> equipes = equipeRepository.findByEventoIdOrderByNomeAsc(eventoId);
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "equipes", key = "'byCategoria:' + #categoriaId")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findByCategoriaId(Long categoriaId) {
        List<EquipeEntity> equipes = equipeRepository.findByCategoriaIdOrderByNomeAsc(categoriaId);
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "equipes", key = "'ativas'")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findEquipesAtivas() {
        List<EquipeEntity> equipes = equipeRepository.findByAtivaTrue();
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "equipes", key = "'equipesCompletas:' + #eventoId")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findEquipesCompletasByEvento(Long eventoId) {
        List<EquipeEntity> equipes = equipeRepository.findEquipesCompletasByEvento(eventoId);
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "equipes", key = "'byAtleta:' + #atletaId")
    @Transactional(readOnly = true)
    public List<EquipeSummaryDTO> findEquipesByAtleta(Long atletaId) {
        List<EquipeEntity> equipes = equipeRepository.findEquipesByAtleta(atletaId);
        return equipes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    // Mapping methods
    private EquipeResponseDTO convertToResponseDTO(EquipeEntity equipe) {
        // Buscar dados dos atletas diretamente do banco para garantir dados atualizados
        long numeroAtletas = atletaRepository.countAtletasByEquipeId(equipe.getId());
        List<String> nomesAtletas = equipeRepository.findNomesAtletasByEquipeId(equipe.getId());

        return EquipeResponseDTO.builder()
                .id(equipe.getId())
                .nome(equipe.getNome())
                .eventoId(equipe.getEvento() != null ? equipe.getEvento().getId() : null)
                .nomeEvento(equipe.getNomeEvento())
                .categoriaId(equipe.getCategoria() != null ? equipe.getCategoria().getId() : null)
                .nomeCategoria(equipe.getNomeCategoria())
                .capitaoId(equipe.getCapitao() != null ? equipe.getCapitao().getId() : null)
                .nomeCapitao(equipe.getNomeCapitao())
                .descricao(equipe.getDescricao())
                .ativa(equipe.getAtiva())
                .numeroAtletas((int) numeroAtletas)
                .nomesAtletas(nomesAtletas)
                .equipeCompleta(numeroAtletas >= 2)
                .podeAdicionarAtleta(numeroAtletas < 6)
                .todosAtletasAceitaramTermos(equipe.todosAtletasAceitaramTermos())
                .todosAtletasPodemParticipar(equipe.todosAtletasPodemParticipar())
                .todosAtletasCompativeisComCategoria(equipe.todosAtletasCompativeisComCategoria())
                .podeSeInscrever(numeroAtletas >= 2 && equipe.getAtiva() && equipe.todosAtletasAceitaramTermos()
                        && equipe.todosAtletasPodemParticipar() && equipe.todosAtletasCompativeisComCategoria())
                .temInscricao(equipe.temInscricao())
                .inscricaoConfirmada(equipe.inscricaoConfirmada())
                .descricaoCompleta(equipe.getNome() + " (" + numeroAtletas + " atletas)"
                        + (equipe.getCapitao() != null ? " - Capitão: " + equipe.getCapitao().getNomeCompleto() : ""))
                .createdAt(equipe.getCreatedAt())
                .updatedAt(equipe.getUpdatedAt())
                .build();
    }

    private EquipeSummaryDTO convertToSummaryDTO(EquipeEntity equipe) {
        return EquipeSummaryDTO.builder()
                .id(equipe.getId())
                .nome(equipe.getNome())
                .nomeEvento(equipe.getNomeEvento())
                .nomeCategoria(equipe.getNomeCategoria())
                .nomeCapitao(equipe.getNomeCapitao())
                .ativa(equipe.getAtiva())
                .numeroAtletas(equipe.getNumeroAtletas())
                .nomesAtletas(equipe.getNomesAtletas())
                .equipeCompleta(equipe.isEquipeCompleta())
                .podeSeInscrever(equipe.podeSeInscrever())
                .temInscricao(equipe.temInscricao())
                .inscricaoConfirmada(equipe.inscricaoConfirmada())
                .createdAt(equipe.getCreatedAt())
                .build();
    }

    private EquipeEntity convertCreateDTOToEntity(EquipeCreateDTO dto) {
        EquipeEntity.EquipeEntityBuilder builder = EquipeEntity.builder()
                .nome(dto.getNome());

        // TODO: Implementar lógica para obter as entidades pelo ID
        // Por enquanto, cria entidades com apenas o ID definido
        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = new CategoriaEntity();
            categoria.setId(dto.getCategoriaId());
            builder.categoria(categoria);
        }

        // Nota: eventoId deve ser definido via outro meio (contexto de criação)
        // capitaoId e descricao não são necessários para criação administrativa

        return builder.build();
    }

    private EquipeEntity convertInscricaoDTOToEntity(Long eventoId, EquipeInscricaoDTO dto) {
        EquipeEntity.EquipeEntityBuilder builder = EquipeEntity.builder()
                .nome(dto.getNome());

        // Define o evento automaticamente
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        builder.evento(evento);

        // Define a categoria
        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = new CategoriaEntity();
            categoria.setId(dto.getCategoriaId());
            builder.categoria(categoria);
        }

        return builder.build();
    }

    private CategoriaEntity buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));
    }

    /**
     * Atualiza a lista de atletas da equipe.
     * Remove todos os atletas atuais e adiciona os novos.
     * 
     * @param equipe          Equipe a ser atualizada
     * @param novosAtletasIds Lista com os IDs dos novos atletas
     */
    private void atualizarAtletasEquipe(EquipeEntity equipe, List<Long> novosAtletasIds) {
        if (novosAtletasIds == null || novosAtletasIds.isEmpty()) {
            throw new RuntimeException("Lista de atletas não pode estar vazia");
        }

        // Remove todos os atletas atuais
        equipe.getAtletas().clear();

        // Adiciona os novos atletas
        for (Long atletaId : novosAtletasIds) {
            AtletaEntity atleta = new AtletaEntity();
            atleta.setId(atletaId);
            equipe.adicionarAtleta(atleta);
        }
    }

    private void updateEquipeFromUpdateDTO(EquipeEntity equipe, EquipeUpdateDTO dto) {
        equipe.setNome(dto.getNome());

        if (dto.getAtiva() != null) {
            equipe.setAtiva(dto.getAtiva());
        }

        // Update categoria if provided
        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = new CategoriaEntity();
            categoria.setId(dto.getCategoriaId());
            equipe.setCategoria(categoria);
        }

        // Atualiza os atletas se fornecidos
        if (dto.getAtletasIds() != null) {
            atualizarAtletasEquipe(equipe, dto.getAtletasIds());
        }

        // Atualiza o capitão se fornecido
        if (dto.getCapitaoId() != null) {
            AtletaEntity novoCapitao = new AtletaEntity();
            novoCapitao.setId(dto.getCapitaoId());

            // Verifica se o novo capitão está na lista de atletas da equipe
            boolean capitaoNaEquipe = equipe.getAtletas().stream()
                    .anyMatch(atleta -> atleta.getId().equals(dto.getCapitaoId()));

            if (capitaoNaEquipe) {
                equipe.setCapitao(novoCapitao);
            } else {
                throw new RuntimeException("O capitão deve estar na lista de atletas da equipe");
            }
        }
    }

    /**
     * Valida os dados de criação da equipe.
     * 
     * @param dto Dados de criação da equipe
     * @throws RuntimeException se os dados são inválidos
     */
    private void validateEquipeData(EquipeCreateDTO dto) {
        // TODO: Implementar validação administrativa específica
        // Como não temos eventoId no DTO, a validação de nome único
        // deve ser feita em outro contexto ou via outro método

        // TODO: Adicionar validações:
        // - Verificar se a categoria existe
        // - Outras validações administrativas

        // Por enquanto, apenas validações básicas via annotations do DTO
    }

    /**
     * Valida os dados de criação da equipe no contexto de inscrição.
     * 
     * @param eventoId ID do evento onde a equipe será criada
     * @param dto      Dados da equipe para inscrição
     * @throws RuntimeException se os dados são inválidos
     */
    private void validateEquipeInscricaoData(Long eventoId, EquipeInscricaoDTO dto) {
        // Verifica se já existe equipe com o mesmo nome no evento
        if (equipeRepository.existsByNomeAndEventoId(dto.getNome(), eventoId)) {
            throw new RuntimeException("Já existe uma equipe com o nome '" + dto.getNome() + "' neste evento");
        }

        // Validações básicas da lista de atletas
        if (dto.getAtletas() == null || dto.getAtletas().isEmpty()) {
            throw new RuntimeException("Pelo menos um atleta deve ser informado");
        }

        // Verifica se não há atletas duplicados na lista (por CPF)
        long atletasUnicos = dto.getAtletas().stream()
                .map(AtletaCreateDTO::getCpf)
                .filter(cpf -> cpf != null && !cpf.trim().isEmpty())
                .distinct()
                .count();

        long atletasComCpf = dto.getAtletas().stream()
                .map(AtletaCreateDTO::getCpf)
                .filter(cpf -> cpf != null && !cpf.trim().isEmpty())
                .count();

        if (atletasUnicos != atletasComCpf) {
            throw new RuntimeException("Não é possível adicionar atletas com o mesmo CPF na equipe");
        }

        // Se capitão foi informado, verifica se está na lista
        if (dto.getCapitaoCpf() != null && !dto.getAtletas().stream()
                .anyMatch(atleta -> dto.getCapitaoCpf().equals(atleta.getCpf()))) {
            throw new RuntimeException("O capitão deve estar na lista de atletas da equipe");
        }

        // Validação da categoria e tipo de participação
        CategoriaEntity categoria = buscarCategoria(dto.getCategoriaId());

        // Verifica se a categoria é de equipe (não individual)
        if (categoria.isIndividual()) {
            throw new RuntimeException(
                    "Categorias individuais devem usar o endpoint de inscrição de atletas (/atletas), não de equipes. "
                            +
                            "Use POST /evento/{eventoId}/inscricao/atletas para inscrições individuais");
        }

        // Para categorias de equipe, verifica se tem mais de 1 atleta
        if (categoria.isEquipe() && dto.getAtletas().size() == 1) {
            throw new RuntimeException("Equipes devem ter mais de 1 atleta. Para categorias individuais, " +
                    "use o endpoint POST /evento/{eventoId}/inscricao/atletas");
        }

        // Validação da quantidade de atletas baseada na categoria
        if (categoria.getQuantidadeDeAtletasPorEquipe() != null &&
                dto.getAtletas().size() != categoria.getQuantidadeDeAtletasPorEquipe()) {
            throw new RuntimeException("Para esta categoria de equipe, é necessário informar exatamente " +
                    categoria.getQuantidadeDeAtletasPorEquipe() + " atleta(s)");
        }

        // Verifica se todos os atletas são compatíveis com a categoria
        for (AtletaCreateDTO atletaDto : dto.getAtletas()) {
            if (!atletaEhCompativel(atletaDto, categoria)) {
                throw new RuntimeException("O atleta " + atletaDto.getNome() +
                        " não atende aos critérios da categoria (idade/gênero)");
            }
        }
    }

    /**
     * Verifica se um atleta é compatível com uma categoria (idade e gênero).
     */
    private boolean atletaEhCompativel(AtletaCreateDTO atletaDto, CategoriaEntity categoria) {
        // Verificar gênero
        if (categoria.getGenero() != null && !categoria.getGenero().equals(atletaDto.getGenero())) {
            return false;
        }

        // Calcular idade baseada na data de nascimento
        int idade = java.time.Period.between(atletaDto.getDataNascimento(), java.time.LocalDate.now()).getYears();

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
     * Valida os dados de atualização da equipe.
     * 
     * @param equipeId ID da equipe sendo atualizada
     * @param dto      Dados de atualização da equipe
     * @throws RuntimeException se os dados são inválidos
     */
    private void validateEquipeUpdateData(Long equipeId, EquipeUpdateDTO dto) {
        // Busca a equipe atual para obter o evento
        EquipeEntity equipeAtual = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + equipeId));

        // Verifica se já existe outra equipe com o mesmo nome no evento
        if (equipeRepository.existsByNomeAndEventoIdAndIdNot(dto.getNome(), equipeAtual.getEvento().getId(),
                equipeId)) {
            throw new RuntimeException("Já existe outra equipe com o nome '" + dto.getNome() + "' neste evento");
        }

        // Validações dos atletas se foram informados
        if (dto.getAtletasIds() != null) {
            // Verifica se há pelo menos um atleta
            if (dto.getAtletasIds().isEmpty()) {
                throw new RuntimeException("Pelo menos um atleta deve ser informado");
            }

            // Verifica a quantidade específica de atletas para a categoria
            CategoriaEntity categoria = equipeAtual.getCategoria();
            if (categoria != null) {
                // Verifica se é categoria de equipe
                if (categoria.isIndividual()) {
                    throw new RuntimeException("Não é possível atualizar uma equipe associada a categoria individual");
                }

                // Para categorias de equipe, verifica se tem mais de 1 atleta
                if (categoria.isEquipe() && dto.getAtletasIds().size() == 1) {
                    throw new RuntimeException("Equipes devem ter mais de 1 atleta");
                }

                // Verifica quantidade específica da categoria
                if (categoria.getQuantidadeDeAtletasPorEquipe() != null) {
                    if (dto.getAtletasIds().size() != categoria.getQuantidadeDeAtletasPorEquipe()) {
                        throw new RuntimeException("Para esta categoria de equipe, é necessário informar exatamente " +
                                categoria.getQuantidadeDeAtletasPorEquipe() + " atleta(s)");
                    }
                }
            }

            // Verifica se não há atletas duplicados na lista
            long atletasUnicos = dto.getAtletasIds().stream().distinct().count();
            if (atletasUnicos != dto.getAtletasIds().size()) {
                throw new RuntimeException("Não é possível adicionar o mesmo atleta mais de uma vez na equipe");
            }

            // Se capitão foi informado, verifica se está na lista de atletas
            if (dto.getCapitaoId() != null && !dto.getAtletasIds().contains(dto.getCapitaoId())) {
                throw new RuntimeException("O capitão deve estar na lista de atletas da equipe");
            }
        } else {
            // Se atletas não foram informados mas capitão foi, verifica se o capitão atual
            // está na equipe
            if (dto.getCapitaoId() != null) {
                boolean capitaoNaEquipe = equipeAtual.getAtletas().stream()
                        .anyMatch(atleta -> atleta.getId().equals(dto.getCapitaoId()));
                if (!capitaoNaEquipe) {
                    throw new RuntimeException("O capitão informado deve estar na lista atual de atletas da equipe");
                }
            }
        }

        // TODO: Adicionar outras validações:
        // - Verificar se a categoria existe e pertence ao evento
        // - Verificar se todos os atletas existem e podem participar
        // - Verificar se todos os atletas são compatíveis com a categoria
        // - Verificar se nenhum atleta já está inscrito em outra equipe do mesmo evento
        // - Verificar se a equipe pode ser editada (não está em competição)
    }

    /**
     * Cria ou busca atletas existentes baseado nos dados fornecidos.
     * 
     * @param atletasDto Lista de dados dos atletas
     * @param eventoId   ID do evento para associar aos atletas
     * @param equipeId   ID da equipe para associar aos atletas (pode ser null)
     * @return Lista de IDs dos atletas criados/encontrados
     */
    private List<Long> criarOuBuscarAtletas(List<AtletaCreateDTO> atletasDto, Long eventoId, Long equipeId) {
        List<Long> atletasIds = new ArrayList<>();

        for (AtletaCreateDTO atletaDto : atletasDto) {
            // Verifica se já existe um atleta com o mesmo CPF
            if (atletaDto.getCpf() != null && atletaService.existsByCpf(atletaDto.getCpf())) {
                // Atleta já existe, buscar o ID
                var atletaExistente = atletaService.findByCpf(atletaDto.getCpf());
                if (atletaExistente.isPresent()) {
                    atletasIds.add(atletaExistente.get().getId());
                }
            } else {
                // Atleta não existe, criar novo
                if (equipeId != null) {
                    var novoAtleta = atletaService.saveForInscricao(atletaDto, eventoId, equipeId);
                    atletasIds.add(novoAtleta.getId());
                } else {
                    // Criar sem equipe por enquanto, será associado depois
                    var novoAtleta = atletaService.saveForEvento(atletaDto, eventoId);
                    atletasIds.add(novoAtleta.getId());
                }
            }
        }

        return atletasIds;
    }

    /**
     * Cria uma inscrição para a equipe recém-criada.
     * 
     * @param equipe             Equipe para a qual criar a inscrição
     * @param equipeInscricaoDTO Dados da inscrição
     * @return InscricaoEntity criada e salva
     */
    private InscricaoEntity criarInscricaoParaEquipe(EquipeEntity equipe, EquipeInscricaoDTO equipeInscricaoDTO) {
        // Determinar valor da inscrição: usar valor personalizado ou da categoria
        BigDecimal valorInscricao = equipeInscricaoDTO.getValorInscricao() != null
                ? equipeInscricaoDTO.getValorInscricao()
                : (equipe.getCategoria().getValorInscricao() != null
                        ? equipe.getCategoria().getValorInscricao()
                        : BigDecimal.ZERO);

        // Criar a inscrição
        InscricaoEntity inscricao = InscricaoEntity.builder()
                .atleta(null) // Inscrição de equipe, não individual
                .evento(equipe.getEvento())
                .categoria(equipe.getCategoria())
                .equipe(equipe)
                .status(StatusInscricao.PENDENTE)
                .valor(valorInscricao)
                .dataInscricao(LocalDateTime.now())
                .termosAceitos(equipeInscricaoDTO.getTermosAceitos() != null
                        ? equipeInscricaoDTO.getTermosAceitos()
                        : false)
                .codigoDesconto(equipeInscricaoDTO.getCodigoDesconto())
                .build();

        // Salvar inscrição
        inscricao = inscricaoRepository.save(inscricao);

        // Estabelecer relacionamento bidirecional
        equipe.setInscricao(inscricao);
        equipeRepository.save(equipe);

        return inscricao;
    }

}
