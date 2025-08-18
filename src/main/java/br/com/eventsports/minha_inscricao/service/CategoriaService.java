package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.categoria.*;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Cacheable(value = "categorias", key = "#id")
    @Transactional(readOnly = true)
    public CategoriaResponseDTO findById(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        return convertToResponseDTO(categoria);
    }

    @Cacheable(value = "categorias", key = "'all'")
    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findAll() {
        List<CategoriaEntity> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "categorias", key = "#result.id")
    @CacheEvict(value = "categorias", key = "'all'")
    public CategoriaResponseDTO save(Long eventoId, CategoriaCreateDTO categoriaCreateDTO) {
        validateCategoriaData(eventoId, categoriaCreateDTO);
        CategoriaEntity categoria = convertCreateDTOToEntity(eventoId, categoriaCreateDTO);
        CategoriaEntity savedCategoria = categoriaRepository.save(categoria);
        return convertToResponseDTO(savedCategoria);
    }

    @CachePut(value = "categorias", key = "#id")
    @CacheEvict(value = "categorias", key = "'all'")
    public CategoriaResponseDTO update(Long id, CategoriaUpdateDTO categoriaUpdateDTO) {
        validateCategoriaUpdateData(id, categoriaUpdateDTO);
        CategoriaEntity existingCategoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));

        updateCategoriaFromUpdateDTO(existingCategoria, categoriaUpdateDTO);
        CategoriaEntity updatedCategoria = categoriaRepository.save(existingCategoria);
        return convertToResponseDTO(updatedCategoria);
    }

    @CacheEvict(value = "categorias", allEntries = true)
    public void deleteById(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        
        // Verificar se a categoria pode ser deletada
        if (!categoria.getInscricoes().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria que possui inscrições");
        }
        if (!categoria.getEquipes().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria que possui equipes");
        }
        
        categoriaRepository.deleteById(id);
    }

    @Cacheable(value = "categorias", key = "'byEvento:' + #eventoId")
    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findByEventoId(Long eventoId) {
        List<CategoriaEntity> categorias = categoriaRepository.findByEventoIdOrderByNomeAsc(eventoId);
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categorias", key = "'ativas'")
    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findCategoriasAtivas() {
        List<CategoriaEntity> categorias = categoriaRepository.findByAtivaTrue();
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categorias", key = "'ativasByEvento:' + #eventoId")
    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findCategoriasAtivasByEvento(Long eventoId) {
        List<CategoriaEntity> categorias = categoriaRepository.findByEventoIdAndAtivaTrue(eventoId);
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findByTipoParticipacao(TipoParticipacao tipoParticipacao) {
        List<CategoriaEntity> categorias = categoriaRepository.findByTipoParticipacao(tipoParticipacao);
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findByEventoIdAndTipoParticipacao(Long eventoId, TipoParticipacao tipoParticipacao) {
        List<CategoriaEntity> categorias = categoriaRepository.findByEventoIdAndTipoParticipacao(eventoId, tipoParticipacao);
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findByNome(String nome) {
        List<CategoriaEntity> categorias = categoriaRepository.findByNomeContainingIgnoreCase(nome);
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaSummaryDTO> findCategoriasDisponiveis(Long eventoId) {
        List<CategoriaEntity> categorias = categoriaRepository.findCategoriasDisponiveis(eventoId);
        return categorias.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "categorias", key = "#id")
    public CategoriaResponseDTO ativar(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        categoria.ativar();
        CategoriaEntity updatedCategoria = categoriaRepository.save(categoria);
        return convertToResponseDTO(updatedCategoria);
    }

    @CacheEvict(value = "categorias", key = "#id")
    public CategoriaResponseDTO desativar(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        categoria.desativar();
        CategoriaEntity updatedCategoria = categoriaRepository.save(categoria);
        return convertToResponseDTO(updatedCategoria);
    }

    // Mapping methods
    private CategoriaResponseDTO convertToResponseDTO(CategoriaEntity categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .eventoId(categoria.getEvento() != null ? categoria.getEvento().getId() : null)
                .nomeEvento(categoria.getEvento() != null ? categoria.getEvento().getNome() : "")
                .nome(categoria.getNome())
                .descricao(categoria.getDescricao())
                .idadeMinima(categoria.getIdadeMinima())
                .idadeMaxima(categoria.getIdadeMaxima())
                .genero(categoria.getGenero())
                .tipoParticipacao(categoria.getTipoParticipacao())
                .quantidadeDeAtletasPorEquipe(categoria.getQuantidadeDeAtletasPorEquipe())
                .valorInscricao(categoria.getValorInscricao())
                .ativa(categoria.getAtiva())
                .numeroInscricoesAtivas(categoria.getNumeroInscricoesAtivas())
                .numeroEquipesAtivas(categoria.getNumeroEquipesAtivas())
                .totalEquipes(categoria.getTotalEquipes())
                .descricaoCompleta(categoria.getDescricaoCompleta())
                .createdAt(categoria.getCreatedAt())
                .updatedAt(categoria.getUpdatedAt())
                .build();
    }

    private CategoriaSummaryDTO convertToSummaryDTO(CategoriaEntity categoria) {
        return CategoriaSummaryDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .nomeEvento(categoria.getEvento() != null ? categoria.getEvento().getNome() : "")
                .genero(categoria.getGenero())
                .tipoParticipacao(categoria.getTipoParticipacao())
                .quantidadeDeAtletasPorEquipe(categoria.getQuantidadeDeAtletasPorEquipe())
                .valorInscricao(categoria.getValorInscricao())
                .ativa(categoria.getAtiva())
                .numeroInscricoesAtivas(categoria.getNumeroInscricoesAtivas())
                .numeroEquipesAtivas(categoria.getNumeroEquipesAtivas())
                .descricaoCompleta(categoria.getDescricaoCompleta())
                .createdAt(categoria.getCreatedAt())
                .build();
    }

    private CategoriaEntity convertCreateDTOToEntity(Long eventoId, CategoriaCreateDTO dto) {
        CategoriaEntity.CategoriaEntityBuilder builder = CategoriaEntity.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .idadeMinima(dto.getIdadeMinima())
                .idadeMaxima(dto.getIdadeMaxima())
                .genero(dto.getGenero())
                .tipoParticipacao(dto.getTipoParticipacao())
                .quantidadeDeAtletasPorEquipe(dto.getQuantidadeDeAtletasPorEquipe())
                .valorInscricao(dto.getValorInscricao());

        if (dto.getAtiva() != null) {
            builder.ativa(dto.getAtiva());
        }

        // Define o evento
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        builder.evento(evento);

        return builder.build();
    }

    private void updateCategoriaFromUpdateDTO(CategoriaEntity categoria, CategoriaUpdateDTO dto) {
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        categoria.setIdadeMinima(dto.getIdadeMinima());
        categoria.setIdadeMaxima(dto.getIdadeMaxima());
        categoria.setGenero(dto.getGenero());
        categoria.setTipoParticipacao(dto.getTipoParticipacao());
        categoria.setQuantidadeDeAtletasPorEquipe(dto.getQuantidadeDeAtletasPorEquipe());
        
        if (dto.getValorInscricao() != null) {
            categoria.setValorInscricao(dto.getValorInscricao());
        }
        
        if (dto.getAtiva() != null) {
            categoria.setAtiva(dto.getAtiva());
        }
    }

    private void validateCategoriaData(Long eventoId, CategoriaCreateDTO dto) {
        // Verifica se já existe categoria com o mesmo nome no evento
        if (categoriaRepository.existsByNomeAndEventoId(dto.getNome(), eventoId)) {
            throw new RuntimeException("Já existe uma categoria com o nome '" + dto.getNome() + "' neste evento");
        }

        // Validação de idades
        validateIdades(dto.getIdadeMinima(), dto.getIdadeMaxima());

        // Validação de quantidade de atletas por tipo de participação
        validateQuantidadeAtletasPorTipo(dto.getTipoParticipacao(), dto.getQuantidadeDeAtletasPorEquipe());

        // TODO: Adicionar validações:
        // - Verificar se o evento existe e está ativo
        // - Verificar regras específicas do evento
    }

    private void validateCategoriaUpdateData(Long categoriaId, CategoriaUpdateDTO dto) {
        // Busca a categoria atual para obter o evento
        CategoriaEntity categoriaAtual = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));

        // Verifica se já existe outra categoria com o mesmo nome no evento
        if (categoriaRepository.existsByNomeAndEventoIdAndIdNot(dto.getNome(), categoriaAtual.getEvento().getId(), categoriaId)) {
            throw new RuntimeException("Já existe outra categoria com o nome '" + dto.getNome() + "' neste evento");
        }

        // Validação de idades
        validateIdades(dto.getIdadeMinima(), dto.getIdadeMaxima());

        // Validação de quantidade de atletas por tipo de participação
        validateQuantidadeAtletasPorTipo(dto.getTipoParticipacao(), dto.getQuantidadeDeAtletasPorEquipe());

        // Verificar se pode alterar a quantidade de atletas (se já existem equipes/inscrições)
        validateAlteracaoQuantidadeAtletas(categoriaAtual, dto.getQuantidadeDeAtletasPorEquipe());

        // TODO: Adicionar outras validações:
        // - Verificar se a categoria pode ser editada (evento não iniciado)
        // - Verificar regras específicas do evento
    }

    /**
     * Valida se as idades mínima e máxima são consistentes
     */
    private void validateIdades(Integer idadeMinima, Integer idadeMaxima) {
        if (idadeMinima != null && idadeMaxima != null) {
            if (idadeMinima > idadeMaxima) {
                throw new RuntimeException("Idade mínima não pode ser maior que idade máxima");
            }
        }
    }

    /**
     * Valida se a quantidade de atletas é compatível com o tipo de participação
     */
    private void validateQuantidadeAtletasPorTipo(TipoParticipacao tipoParticipacao, Integer quantidadeDeAtletasPorEquipe) {
        if (quantidadeDeAtletasPorEquipe != null && tipoParticipacao != null) {
            if (tipoParticipacao == TipoParticipacao.INDIVIDUAL) {
                if (quantidadeDeAtletasPorEquipe != 1) {
                    throw new RuntimeException("Para categoria individual, a quantidade de atletas deve ser exatamente 1");
                }
            } else if (tipoParticipacao == TipoParticipacao.EQUIPE) {
                if (quantidadeDeAtletasPorEquipe < 2) {
                    throw new RuntimeException("Para categoria em equipe, a quantidade de atletas deve ser pelo menos 2");
                }
                if (quantidadeDeAtletasPorEquipe > 6) {
                    throw new RuntimeException("Para categoria em equipe, a quantidade de atletas não pode ser maior que 6");
                }
            }
        }
    }

    /**
     * Valida se é possível alterar a quantidade de atletas de uma categoria
     */
    private void validateAlteracaoQuantidadeAtletas(CategoriaEntity categoriaAtual, Integer novaQuantidade) {
        if (novaQuantidade != null && 
            !novaQuantidade.equals(categoriaAtual.getQuantidadeDeAtletasPorEquipe())) {
            
            if (!categoriaAtual.getEquipes().isEmpty()) {
                throw new RuntimeException("Não é possível alterar a quantidade de atletas de uma categoria que já possui equipes");
            }
            if (!categoriaAtual.getInscricoes().isEmpty()) {
                throw new RuntimeException("Não é possível alterar a quantidade de atletas de uma categoria que já possui inscrições");
            }
        }
    }
}
