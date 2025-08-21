package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.atleta.AtletaCreateDTO;
import br.com.eventsports.minha_inscricao.dto.inscricao.*;
import br.com.eventsports.minha_inscricao.entity.*;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import br.com.eventsports.minha_inscricao.repository.*;
import br.com.eventsports.minha_inscricao.service.Interfaces.IInscricaoService;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InscricaoService implements IInscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final EquipeRepository equipeRepository;

    @Cacheable(value = "inscricoes", key = "#id")
    @Transactional(readOnly = true)
    public InscricaoResponseDTO findById(Long id) {
        InscricaoEntity inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada com ID: " + id));
        return convertToResponseDTO(inscricao);
    }

    @Cacheable(value = "inscricoes", key = "'all'")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findAll() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findAll();
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "inscricoes", key = "#result.id")
    @CacheEvict(value = "inscricoes", key = "'all'")
    public InscricaoResponseDTO save(InscricaoCreateDTO inscricaoCreateDTO) {
        validateInscricaoData(inscricaoCreateDTO, null);
        InscricaoEntity inscricao = convertCreateDTOToEntity(inscricaoCreateDTO, null);
        InscricaoEntity savedInscricao = inscricaoRepository.save(inscricao);
        return convertToResponseDTO(savedInscricao);
    }

    @CachePut(value = "inscricoes", key = "#result.id")
    @CacheEvict(value = "inscricoes", key = "'all'")
    public InscricaoResponseDTO saveForEvento(InscricaoCreateDTO inscricaoCreateDTO, Long eventoId) {
        validateInscricaoData(inscricaoCreateDTO, eventoId);
        InscricaoEntity inscricao = convertCreateDTOToEntity(inscricaoCreateDTO, eventoId);
        InscricaoEntity savedInscricao = inscricaoRepository.save(inscricao);
        return convertToResponseDTO(savedInscricao);
    }

    @CachePut(value = "inscricoes", key = "#result.id")
    @CacheEvict(value = "inscricoes", key = "'all'")
    public InscricaoResponseDTO saveComEquipeForEvento(InscricaoComEquipeCreateDTO inscricaoComEquipeDTO, Long eventoId) {
        validateInscricaoComEquipeData(inscricaoComEquipeDTO, eventoId);
        
        // 1. Criar os atletas primeiro
        List<Long> atletasIds = criarAtletasParaInscricao(inscricaoComEquipeDTO.getAtletas(), eventoId);
        
        // 2. Criar a equipe com os atletas criados
        Long equipeId = criarEquipeParaInscricao(inscricaoComEquipeDTO, eventoId, atletasIds);
        
        // 3. Criar a inscrição vinculada à equipe
        InscricaoEntity inscricao = criarInscricaoComEquipe(inscricaoComEquipeDTO, eventoId, equipeId);
        
        // 4. Salvar a inscrição
        InscricaoEntity savedInscricao = inscricaoRepository.save(inscricao);
        
        return convertToResponseDTO(savedInscricao);
    }

    @CachePut(value = "inscricoes", key = "#id")
    @CacheEvict(value = "inscricoes", key = "'all'")
    public InscricaoResponseDTO update(Long id, InscricaoUpdateDTO inscricaoUpdateDTO) {
        InscricaoEntity inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada com ID: " + id));
        
        updateInscricaoFromDTO(inscricao, inscricaoUpdateDTO);
        InscricaoEntity updatedInscricao = inscricaoRepository.save(inscricao);
        return convertToResponseDTO(updatedInscricao);
    }

    @CacheEvict(value = "inscricoes", allEntries = true)
    public void deleteById(Long id) {
        if (!inscricaoRepository.existsById(id)) {
            throw new RuntimeException("Inscrição não encontrada com ID: " + id);
        }
        inscricaoRepository.deleteById(id);
    }

    @Cacheable(value = "inscricoes", key = "'byEvento:' + #eventoId")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findByEventoId(Long eventoId) {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findByEventoId(eventoId);
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "inscricoes", key = "'byCategoria:' + #categoriaId")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findByCategoriaId(Long categoriaId) {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findByCategoriaId(categoriaId);
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "inscricoes", key = "'byEquipe:' + #equipeId")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findByEquipeId(Long equipeId) {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findByEquipeId(equipeId);
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "inscricoes", key = "'byStatus:' + #status")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findByStatus(StatusInscricao status) {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findByStatus(status);
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "inscricoes", key = "'confirmadas'")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findInscricoesConfirmadas() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findInscricoesConfirmadas();
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "inscricoes", key = "'pendentes'")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findInscricoesPendentes() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findInscricoesPendentes();
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "inscricoes", key = "'canceladas'")
    @Transactional(readOnly = true)
    public List<InscricaoSummaryDTO> findInscricoesCanceladas() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findInscricoesCanceladas();
        return inscricoes.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "inscricoes", allEntries = true)
    public InscricaoResponseDTO confirmar(Long id) {
        InscricaoEntity inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada com ID: " + id));
        
        inscricao.confirmar();
        InscricaoEntity updatedInscricao = inscricaoRepository.save(inscricao);
        return convertToResponseDTO(updatedInscricao);
    }

    @CacheEvict(value = "inscricoes", allEntries = true)
    public InscricaoResponseDTO cancelar(Long id, String motivo) {
        InscricaoEntity inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada com ID: " + id));
        
        inscricao.cancelar(motivo);
        InscricaoEntity updatedInscricao = inscricaoRepository.save(inscricao);
        return convertToResponseDTO(updatedInscricao);
    }

    @CacheEvict(value = "inscricoes", allEntries = true)
    public InscricaoResponseDTO colocarEmListaEspera(Long id) {
        InscricaoEntity inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada com ID: " + id));
        
        inscricao.colocarEmListaEspera();
        InscricaoEntity updatedInscricao = inscricaoRepository.save(inscricao);
        return convertToResponseDTO(updatedInscricao);
    }

    @Transactional(readOnly = true)
    public long countByEventoIdAndStatus(Long eventoId, StatusInscricao status) {
        return inscricaoRepository.countByEventoIdAndStatus(eventoId, status);
    }

    @Transactional(readOnly = true)
    public long countByCategoriaIdAndStatus(Long categoriaId, StatusInscricao status) {
        return inscricaoRepository.countByCategoriaIdAndStatus(categoriaId, status);
    }

    // Métodos de conversão
    private InscricaoResponseDTO convertToResponseDTO(InscricaoEntity inscricao) {
        List<AtletaSummaryDTO> atletasDTO = inscricao.getAtleta() != null 
                ? List.of(convertUsuarioToAtletaSummaryDTO(inscricao.getAtleta()))
                : List.of();

        return InscricaoResponseDTO.builder()
                .id(inscricao.getId())
                .atletas(atletasDTO)
                .eventoId(inscricao.getEvento() != null ? inscricao.getEvento().getId() : null)
                .nomeEvento(inscricao.getNomeEvento())
                .categoriaId(inscricao.getCategoria() != null ? inscricao.getCategoria().getId() : null)
                .nomeCategoria(inscricao.getNomeCategoria())
                .equipeId(inscricao.getEquipe() != null ? inscricao.getEquipe().getId() : null)
                .nomeEquipe(inscricao.getEquipe() != null ? inscricao.getEquipe().getNome() : null)
                .status(inscricao.getStatus())
                .descricaoStatus(inscricao.getDescricaoStatus())
                .valor(inscricao.getValor())
                .dataInscricao(inscricao.getDataInscricao())
                .dataConfirmacao(inscricao.getDataConfirmacao())
                .dataCancelamento(inscricao.getDataCancelamento())
                .termosAceitos(inscricao.getTermosAceitos())
                .codigoDesconto(inscricao.getCodigoDesconto())
                .valorDesconto(inscricao.getValorDesconto())
                .motivoCancelamento(inscricao.getMotivoCancelamento())
                .createdAt(inscricao.getCreatedAt())
                .updatedAt(inscricao.getUpdatedAt())
                .valorTotal(inscricao.getValorTotal())
                .temDesconto(inscricao.temDesconto())
                .podeSerCancelada(inscricao.podeSerCancelada())
                .precisaPagamento(inscricao.precisaPagamento())
                .ativa(inscricao.isAtiva())
                .tipoInscricao(inscricao.getTipoInscricao())
                .numeroParticipantes(inscricao.getNumeroParticipantes())
                .nomeParticipante(inscricao.getNomeParticipante())
                .temPagamento(inscricao.getPagamento() != null)
                .build();
    }

    private InscricaoSummaryDTO convertToSummaryDTO(InscricaoEntity inscricao) {
        return InscricaoSummaryDTO.builder()
                .id(inscricao.getId())
                .nomeEvento(inscricao.getNomeEvento())
                .nomeCategoria(inscricao.getNomeCategoria())
                .nomeEquipe(inscricao.getEquipe() != null ? inscricao.getEquipe().getNome() : null)
                .status(inscricao.getStatus())
                .descricaoStatus(inscricao.getDescricaoStatus())
                .valorTotal(inscricao.getValorTotal())
                .dataInscricao(inscricao.getDataInscricao())
                .dataConfirmacao(inscricao.getDataConfirmacao())
                .tipoInscricao(inscricao.getTipoInscricao())
                .numeroParticipantes(inscricao.getNumeroParticipantes())
                .nomeParticipante(inscricao.getNomeParticipante())
                .ativa(inscricao.isAtiva())
                .podeSerCancelada(inscricao.podeSerCancelada())
                .precisaPagamento(inscricao.precisaPagamento())
                .build();
    }

    private AtletaSummaryDTO convertAtletaToSummaryDTO(AtletaEntity atleta) {
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

    private AtletaSummaryDTO convertUsuarioToAtletaSummaryDTO(UsuarioEntity usuario) {
        return AtletaSummaryDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .dataNascimento(usuario.getDataNascimento())
                .genero(usuario.getGenero())
                .telefone(usuario.getTelefone())
                .aceitaTermos(usuario.getAceitaTermos())
                .idade(usuario.getIdade())
                .podeParticipar(usuario.podeParticipar())
                .nomeEvento("") // Não aplicável diretamente
                .statusInscricao("") // Será obtido da inscrição
                .nomeEquipe("") // Não aplicável diretamente
                .build();
    }

    private InscricaoEntity convertCreateDTOToEntity(InscricaoCreateDTO dto, Long eventoId) {
        // Definir evento - usa o eventoId fornecido como parâmetro
        if (eventoId == null) {
            throw new RuntimeException("Evento é obrigatório para criar inscrição");
        }
        
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));

        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + dto.getCategoriaId()));

        // Buscar valor da categoria automaticamente
        BigDecimal valorInscricao = categoria.getValorInscricao();
        if (valorInscricao == null) {
            throw new RuntimeException("Categoria não possui valor de inscrição definido");
        }

        // Aplicar desconto se houver
        BigDecimal valorFinal = aplicarDesconto(valorInscricao, dto.getCodigoDesconto(), dto.getValorDesconto());

        InscricaoEntity inscricao = InscricaoEntity.builder()
                .valor(valorFinal)
                .termosAceitos(dto.getTermosAceitos())
                .codigoDesconto(dto.getCodigoDesconto())
                .valorDesconto(dto.getValorDesconto())
                .build();

        inscricao.setEvento(evento);
        inscricao.setCategoria(categoria);

        // Nota: Equipe e atletas serão definidos em métodos específicos
        // O DTO simples não contém mais essas informações

        return inscricao;
    }

    private void updateInscricaoFromDTO(InscricaoEntity inscricao, InscricaoUpdateDTO dto) {
        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + dto.getCategoriaId()));
            inscricao.setCategoria(categoria);
        }

        // Nota: Equipe e atletas não são mais atualizados via DTO simples
        // Essas operações devem ser feitas via métodos específicos

        if (dto.getStatus() != null) {
            inscricao.setStatus(dto.getStatus());
        }
        if (dto.getValor() != null) {
            inscricao.setValor(dto.getValor());
        }
        if (dto.getTermosAceitos() != null) {
            inscricao.setTermosAceitos(dto.getTermosAceitos());
        }
        if (dto.getCodigoDesconto() != null) {
            inscricao.setCodigoDesconto(dto.getCodigoDesconto());
        }
        if (dto.getValorDesconto() != null) {
            inscricao.setValorDesconto(dto.getValorDesconto());
        }
        if (dto.getMotivoCancelamento() != null) {
            inscricao.setMotivoCancelamento(dto.getMotivoCancelamento());
        }
    }

    private void validateInscricaoData(InscricaoCreateDTO dto, Long eventoId) {
        // Validar aceite dos termos
        if (dto.getTermosAceitos() == null || !dto.getTermosAceitos()) {
            throw new RuntimeException("É obrigatório aceitar os termos para realizar a inscrição");
        }

        // Validar se evento aceita inscrições
        if (eventoId == null) {
            throw new RuntimeException("Evento é obrigatório para validar inscrição");
        }
        
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        
        if (!evento.podeReceberInscricoes()) {
            throw new RuntimeException("Este evento não está aceitando inscrições no momento");
        }
    }

    private void validateInscricaoComEquipeData(InscricaoComEquipeCreateDTO dto, Long eventoId) {
        // Validar aceite dos termos
        if (dto.getTermosAceitos() == null || !dto.getTermosAceitos()) {
            throw new RuntimeException("É obrigatório aceitar os termos para realizar a inscrição");
        }

        // Validar se evento aceita inscrições
        if (eventoId == null) {
            throw new RuntimeException("Evento é obrigatório para validar inscrição");
        }
        
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        
        if (!evento.podeReceberInscricoes()) {
            throw new RuntimeException("Este evento não está aceitando inscrições no momento");
        }

        // Validar atletas
        if (dto.getAtletas() == null || dto.getAtletas().isEmpty()) {
            throw new RuntimeException("Pelo menos um atleta deve ser informado");
        }

        // Validar índice do capitão
        if (dto.getIndiceCapitao() != null) {
            if (dto.getIndiceCapitao() < 0 || dto.getIndiceCapitao() >= dto.getAtletas().size()) {
                throw new RuntimeException("Índice do capitão deve estar entre 0 e " + (dto.getAtletas().size() - 1));
            }
        }
    }

    private List<Long> criarAtletasParaInscricao(List<AtletaCreateDTO> atletasDTO, Long eventoId) {
        // TODO: Implementar criação direta de atletas ou usar service sem dependência circular
        // Por enquanto, retorna IDs fictícios ou implemente a lógica diretamente aqui
        throw new RuntimeException("Método ainda não implementado - requer refatoração para evitar dependência circular");
    }

    private Long criarEquipeParaInscricao(InscricaoComEquipeCreateDTO dto, Long eventoId, List<Long> atletasIds) {
        // TODO: Implementar criação direta de equipe ou usar service sem dependência circular  
        // Por enquanto, retorna ID fictício ou implemente a lógica diretamente aqui
        throw new RuntimeException("Método ainda não implementado - requer refatoração para evitar dependência circular");
    }

    private InscricaoEntity criarInscricaoComEquipe(InscricaoComEquipeCreateDTO dto, Long eventoId, Long equipeId) {
        // Definir evento
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));

        // Definir categoria
        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + dto.getCategoriaId()));

        // Buscar valor da categoria automaticamente
        BigDecimal valorInscricao = categoria.getValorInscricao();
        if (valorInscricao == null) {
            throw new RuntimeException("Categoria não possui valor de inscrição definido");
        }

        // Aplicar desconto se houver
        BigDecimal valorFinal = aplicarDesconto(valorInscricao, dto.getCodigoDesconto(), dto.getValorDesconto());

        InscricaoEntity inscricao = InscricaoEntity.builder()
                .valor(valorFinal)
                .termosAceitos(dto.getTermosAceitos())
                .codigoDesconto(dto.getCodigoDesconto())
                .valorDesconto(dto.getValorDesconto())
                .build();

        inscricao.setEvento(evento);
        inscricao.setCategoria(categoria);

        // Definir equipe
        EquipeEntity equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + equipeId));
        inscricao.setEquipe(equipe);

        return inscricao;
    }

    /**
     * Aplica desconto no valor base da inscrição
     */
    private BigDecimal aplicarDesconto(BigDecimal valorBase, String codigoDesconto, BigDecimal valorDesconto) {
        if (valorDesconto == null || valorDesconto.compareTo(BigDecimal.ZERO) <= 0) {
            return valorBase;
        }

        BigDecimal valorFinal = valorBase.subtract(valorDesconto);
        
        // Garantir que o valor final não seja negativo
        if (valorFinal.compareTo(BigDecimal.ZERO) < 0) {
            valorFinal = BigDecimal.ZERO;
        }

        return valorFinal;
    }
}
