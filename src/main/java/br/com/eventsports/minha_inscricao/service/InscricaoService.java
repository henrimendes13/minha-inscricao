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
                ? List.of(convertAtletaToSummaryDTO(inscricao.getAtleta()))
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
                .usuarioInscricaoId(inscricao.getUsuarioInscricao() != null ? inscricao.getUsuarioInscricao().getId() : null)
                .nomeUsuarioInscricao(inscricao.getNomeUsuarioInscricao())
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

}
