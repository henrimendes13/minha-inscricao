package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.equipe.*;
import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;

import br.com.eventsports.minha_inscricao.repository.EquipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;

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
    @CachePut(value = "equipes", key = "#result.id")
    @CacheEvict(value = "equipes", key = "'all'")
    public EquipeResponseDTO criarEquipeParaInscricao(Long eventoId, EquipeInscricaoDTO equipeInscricaoDTO) {
        return criarEquipeParaInscricao(eventoId, equipeInscricaoDTO, null);
    }

    /**
     * Cria uma equipe no contexto de inscrição em um evento específico.
     * @param eventoId ID do evento
     * @param equipeInscricaoDTO Dados da equipe
     * @param usuarioLogadoId ID do usuário logado (null para usar o primeiro da lista)
     */
    @CachePut(value = "equipes", key = "#result.id")
    @CacheEvict(value = "equipes", key = "'all'")
    public EquipeResponseDTO criarEquipeParaInscricao(Long eventoId, EquipeInscricaoDTO equipeInscricaoDTO, Long usuarioLogadoId) {
        validateEquipeInscricaoData(eventoId, equipeInscricaoDTO);
        
        // Busca a categoria para determinar o tipo de participação
        CategoriaEntity categoria = buscarCategoria(equipeInscricaoDTO.getCategoriaId());
        
        // Ajusta a lista de atletas baseado no tipo da categoria
        List<Long> atletasFinais = ajustarAtletasParaCategoria(categoria, equipeInscricaoDTO.getAtletasIds(), usuarioLogadoId);
        
        EquipeEntity equipe = convertInscricaoDTOToEntity(eventoId, equipeInscricaoDTO);
        
        // Adiciona os atletas à equipe
        adicionarAtletasAEquipe(equipe, atletasFinais);
        
        // Define o capitão baseado no tipo da categoria
        definirCapitaoParaCategoria(equipe, categoria, atletasFinais, equipeInscricaoDTO.getCapitaoId());
        
        EquipeEntity savedEquipe = equipeRepository.save(equipe);
        
        // TODO: Criar InscricaoEntity associada à equipe
        
        return convertToResponseDTO(savedEquipe);
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
        if (!equipeRepository.existsById(id)) {
            throw new RuntimeException("Equipe não encontrada com ID: " + id);
        }
        equipeRepository.deleteById(id);
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
                .numeroAtletas(equipe.getNumeroAtletas())
                .nomesAtletas(equipe.getNomesAtletas())
                .equipeCompleta(equipe.isEquipeCompleta())
                .podeAdicionarAtleta(equipe.podeAdicionarAtleta())
                .todosAtletasAceitaramTermos(equipe.todosAtletasAceitaramTermos())
                .todosAtletasPodemParticipar(equipe.todosAtletasPodemParticipar())
                .todosAtletasCompativeisComCategoria(equipe.todosAtletasCompativeisComCategoria())
                .podeSeInscrever(equipe.podeSeInscrever())
                .temInscricao(equipe.temInscricao())
                .inscricaoConfirmada(equipe.inscricaoConfirmada())
                .descricaoCompleta(equipe.getDescricaoCompleta())
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
        // TODO: Implementar busca real da categoria via repository
        // Por enquanto, cria uma instância com apenas o ID
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(categoriaId);
        return categoria;
    }

    private List<Long> ajustarAtletasParaCategoria(CategoriaEntity categoria, List<Long> atletasOriginais, Long usuarioLogadoId) {
        // TODO: Implementar busca real da categoria para verificar tipoParticipacao
        // Por enquanto, assumindo que a categoria já tem o tipo definido
        
        if (categoria.isIndividual()) {
            // Para categoria individual, sempre deve ter exatamente 1 atleta
            if (usuarioLogadoId != null) {
                return List.of(usuarioLogadoId);
            } else {
                // Se não tem usuário logado, usa o primeiro da lista (fallback)
                if (atletasOriginais != null && !atletasOriginais.isEmpty()) {
                    return List.of(atletasOriginais.get(0));
                } else {
                    throw new RuntimeException("Para categoria individual, é necessário informar exatamente 1 atleta");
                }
            }
        } else {
            // Para categoria em equipe, verifica a quantidade específica definida na categoria
            Integer quantidadeEsperada = categoria.getQuantidadeDeAtletasPorEquipe();
            
            if (quantidadeEsperada == null) {
                // Fallback para validação antiga se não foi definido
                if (atletasOriginais == null || atletasOriginais.size() < 2) {
                    throw new RuntimeException("Para categoria em equipe, é necessário informar pelo menos 2 atletas");
                }
                if (atletasOriginais.size() > 6) {
                    throw new RuntimeException("Para categoria em equipe, é possível informar no máximo 6 atletas");
                }
            } else {
                // Validação baseada na quantidade específica da categoria
                if (atletasOriginais == null || atletasOriginais.size() != quantidadeEsperada) {
                    throw new RuntimeException("Para esta categoria, é necessário informar exatamente " + 
                                             quantidadeEsperada + " atleta(s)");
                }
            }
            
            return new ArrayList<>(atletasOriginais);
        }
    }

    private void definirCapitaoParaCategoria(EquipeEntity equipe, CategoriaEntity categoria, List<Long> atletasIds, Long capitaoIdSugerido) {
        Long capitaoId;
        
        if (categoria.isIndividual()) {
            // Para categoria individual, o capitão é o próprio atleta
            capitaoId = atletasIds.get(0);
        } else {
            // Para categoria em equipe
            if (capitaoIdSugerido != null && atletasIds.contains(capitaoIdSugerido)) {
                capitaoId = capitaoIdSugerido;
            } else {
                // Se não foi sugerido ou não está na lista, usa o primeiro
                capitaoId = atletasIds.get(0);
            }
        }
        
        AtletaEntity capitao = new AtletaEntity();
        capitao.setId(capitaoId);
        equipe.setCapitao(capitao);
    }

    private void adicionarAtletasAEquipe(EquipeEntity equipe, List<Long> atletasIds) {
        if (atletasIds == null || atletasIds.isEmpty()) {
            throw new RuntimeException("Lista de atletas não pode estar vazia");
        }

        for (Long atletaId : atletasIds) {
            AtletaEntity atleta = new AtletaEntity();
            atleta.setId(atletaId);
            equipe.adicionarAtleta(atleta);
        }
    }

    /**
     * Atualiza a lista de atletas da equipe.
     * Remove todos os atletas atuais e adiciona os novos.
     * @param equipe Equipe a ser atualizada
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
     * @param eventoId ID do evento onde a equipe será criada
     * @param dto Dados da equipe para inscrição
     * @throws RuntimeException se os dados são inválidos
     */
    private void validateEquipeInscricaoData(Long eventoId, EquipeInscricaoDTO dto) {
        // Verifica se já existe equipe com o mesmo nome no evento
        if (equipeRepository.existsByNomeAndEventoId(dto.getNome(), eventoId)) {
            throw new RuntimeException("Já existe uma equipe com o nome '" + dto.getNome() + "' neste evento");
        }

        // Validações básicas da lista de atletas
        if (dto.getAtletasIds() == null || dto.getAtletasIds().isEmpty()) {
            throw new RuntimeException("Pelo menos um atleta deve ser informado");
        }

        // Verifica se não há atletas duplicados na lista
        long atletasUnicos = dto.getAtletasIds().stream().distinct().count();
        if (atletasUnicos != dto.getAtletasIds().size()) {
            throw new RuntimeException("Não é possível adicionar o mesmo atleta mais de uma vez na equipe");
        }

        // Se capitão foi informado, verifica se está na lista
        if (dto.getCapitaoId() != null && !dto.getAtletasIds().contains(dto.getCapitaoId())) {
            throw new RuntimeException("O capitão deve estar na lista de atletas da equipe");
        }

        // Validação da quantidade de atletas baseada na categoria
        CategoriaEntity categoria = buscarCategoria(dto.getCategoriaId());
        if (!categoria.listaAtletasTemQuantidadeCorreta(dto.getAtletasIds())) {
            Integer quantidadeEsperada = categoria.getQuantidadeDeAtletasPorEquipe();
            if (quantidadeEsperada != null) {
                throw new RuntimeException("Para esta categoria, é necessário informar exatamente " + 
                                         quantidadeEsperada + " atleta(s)");
            }
        }

        // TODO: Adicionar outras validações:
        // - Verificar se o evento existe e pode receber inscrições
        // - Verificar se a categoria existe, pertence ao evento
        // - Verificar se todos os atletas existem e podem participar
        // - Verificar se todos os atletas são compatíveis com a categoria
        // - Verificar se nenhum atleta já está inscrito em outra equipe do mesmo evento
    }

    /**
     * Valida os dados de atualização da equipe.
     * @param equipeId ID da equipe sendo atualizada
     * @param dto Dados de atualização da equipe
     * @throws RuntimeException se os dados são inválidos
     */
    private void validateEquipeUpdateData(Long equipeId, EquipeUpdateDTO dto) {
        // Busca a equipe atual para obter o evento
        EquipeEntity equipeAtual = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + equipeId));

        // Verifica se já existe outra equipe com o mesmo nome no evento
        if (equipeRepository.existsByNomeAndEventoIdAndIdNot(dto.getNome(), equipeAtual.getEvento().getId(), equipeId)) {
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
            if (categoria != null && categoria.getQuantidadeDeAtletasPorEquipe() != null) {
                if (dto.getAtletasIds().size() != categoria.getQuantidadeDeAtletasPorEquipe()) {
                    throw new RuntimeException("Para esta categoria, é necessário informar exatamente " + 
                                             categoria.getQuantidadeDeAtletasPorEquipe() + " atleta(s)");
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
            // Se atletas não foram informados mas capitão foi, verifica se o capitão atual está na equipe
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
}
