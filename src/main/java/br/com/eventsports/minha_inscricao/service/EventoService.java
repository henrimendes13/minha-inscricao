package br.com.eventsports.minha_inscricao.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.eventsports.minha_inscricao.dto.evento.EventoCreateDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoResponseDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoUpdateDTO;
import br.com.eventsports.minha_inscricao.dto.evento.StatusChangeDTO;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.StatusEvento;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.exception.InvalidDateRangeException;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IEventoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EventoService implements IEventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    @Cacheable(value = "eventos-dto", key = "#id")
    @Transactional(readOnly = true)
    public EventoResponseDTO findById(Long id) {
        EventoEntity evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + id));
        return convertToResponseDTO(evento);
    }

    @Cacheable(value = "eventos-dto", key = "'all'")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findAll() {
        List<EventoEntity> eventos = eventoRepository.findAll();
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "eventos-dto", key = "#result.id")
    @CacheEvict(value = "eventos-dto", key = "'all'")
    public EventoResponseDTO save(EventoCreateDTO eventoCreateDTO) {
        validateDateRange(eventoCreateDTO.getDataInicioDoEvento(), eventoCreateDTO.getDataFimDoEvento());
        EventoEntity evento = convertCreateDTOToEntity(eventoCreateDTO);
        EventoEntity savedEvento = eventoRepository.save(evento);
        return convertToResponseDTO(savedEvento);
    }

    @CachePut(value = "eventos-dto", key = "#id")
    @CacheEvict(value = "eventos-dto", key = "'all'")
    public EventoResponseDTO update(Long id, EventoUpdateDTO eventoUpdateDTO) {
        validateDateRange(eventoUpdateDTO.getDataInicioDoEvento(), eventoUpdateDTO.getDataFimDoEvento());
        EventoEntity existingEvento = eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + id));

        updateEventoFromUpdateDTO(existingEvento, eventoUpdateDTO);
        EventoEntity updatedEvento = eventoRepository.save(existingEvento);
        return convertToResponseDTO(updatedEvento);
    }

    @CacheEvict(value = "eventos-dto", allEntries = true)
    public void deleteById(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new EventoNotFoundException("Evento não encontrado com ID: " + id);
        }
        eventoRepository.deleteById(id);
    }

    @Cacheable(value = "eventos-dto", key = "'search:' + #nome")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findByNome(String nome) {
        List<EventoEntity> eventos = eventoRepository.findByNomeContainingIgnoreCase(nome);
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "eventos-dto", key = "'upcoming'")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findEventosUpcoming() {
        List<EventoEntity> eventos = eventoRepository.findEventosUpcoming();
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "eventos-dto", key = "'past'")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findEventosPast() {
        List<EventoEntity> eventos = eventoRepository.findEventosPast();
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "eventos-dto", key = "'between:' + #inicio + ':' + #fim")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findEventosByDataBetween(LocalDateTime inicio, LocalDateTime fim) {
        List<EventoEntity> eventos = eventoRepository.findEventosByDataBetween(inicio, fim);
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    // Mapping methods
    private EventoResponseDTO convertToResponseDTO(EventoEntity evento) {
        return EventoResponseDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .dataInicioDoEvento(evento.getDataInicioDoEvento())
                .dataFimDoEvento(evento.getDataFimDoEvento())
                .status(evento.getStatus() != null ? evento.getStatus().name() : null)
                .descricaoStatus(evento.getDescricaoStatus())
                .nomeOrganizador(evento.getNomeOrganizador())
                .descricao(evento.getDescricao())
                .cidade(evento.getCidade())
                .estado(evento.getEstado())
                .endereco(evento.getEndereco())
                .totalCategorias(evento.getTotalCategorias())
                .totalInscricoes(evento.getTotalInscricoes())
                .inscricoesAtivas(evento.getInscricoesAtivas())
                .podeReceberInscricoes(evento.podeReceberInscricoes())
                .podeSerEditado(evento.podeSerEditado())
                .createdAt(evento.getCreatedAt())
                .updatedAt(evento.getUpdatedAt())
                .build();
    }

    private EventoSummaryDTO convertToSummaryDTO(EventoEntity evento) {
        return EventoSummaryDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .dataInicioDoEvento(evento.getDataInicioDoEvento())
                .dataFimDoEvento(evento.getDataFimDoEvento())
                .status(evento.getStatus() != null ? evento.getStatus().name() : null)
                .descricaoStatus(evento.getDescricaoStatus())
                .nomeOrganizador(evento.getNomeOrganizador())
                .totalCategorias(evento.getTotalCategorias())
                .inscricoesAtivas(evento.getInscricoesAtivas())
                .cidade(evento.getCidade())
                .estado(evento.getEstado())
                .podeReceberInscricoes(evento.podeReceberInscricoes())
                .createdAt(evento.getCreatedAt())
                .build();
    }

    private EventoEntity convertCreateDTOToEntity(EventoCreateDTO dto) {
        // Convert LocalDate to LocalDateTime (start of day for inicio, end of day for
        // fim)
        LocalDateTime inicioDateTime = dto.getDataInicioDoEvento().atStartOfDay();
        LocalDateTime fimDateTime = dto.getDataFimDoEvento().atTime(23, 59, 59);

        EventoEntity.EventoEntityBuilder builder = EventoEntity.builder()
                .nome(dto.getNome())
                .dataInicioDoEvento(inicioDateTime)
                .dataFimDoEvento(fimDateTime)
                .descricao(dto.getDescricao())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .endereco(dto.getEndereco());

        // Obter o organizador do usuário autenticado
        UsuarioEntity organizador = getUsuarioAutenticado();
        if (organizador != null) {
            builder.organizador(organizador);
        }

        return builder.build();
    }

    private void updateEventoFromUpdateDTO(EventoEntity evento, EventoUpdateDTO dto) {
        // Convert LocalDate to LocalDateTime (start of day for inicio, end of day for
        // fim)
        LocalDateTime inicioDateTime = dto.getDataInicioDoEvento().atStartOfDay();
        LocalDateTime fimDateTime = dto.getDataFimDoEvento().atTime(23, 59, 59);

        evento.setNome(dto.getNome());
        evento.setDataInicioDoEvento(inicioDateTime);
        evento.setDataFimDoEvento(fimDateTime);
        evento.setDescricao(dto.getDescricao());
        evento.setCidade(dto.getCidade());
        evento.setEstado(dto.getEstado());
        evento.setEndereco(dto.getEndereco());
    }

    /**
     * Valida se a data de fim é igual ou posterior à data de início do evento.
     * 
     * @param dataInicio Data de início do evento
     * @param dataFim    Data de fim do evento
     * @throws InvalidDateRangeException se a data de fim for anterior à data
     *                                   de início
     */
    private void validateDateRange(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            return; // Deixa as validações @NotNull dos DTOs cuidarem disso
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new InvalidDateRangeException("A data de fim do evento deve ser igual ou posterior à data de início");
        }
    }

    /**
     * Valida se a data de fim é igual ou posterior à data de início do evento (versão para
     * LocalDate).
     * 
     * @param dataInicio Data de início do evento
     * @param dataFim    Data de fim do evento
     * @throws InvalidDateRangeException se a data de fim for anterior à data
     *                                   de início
     */
    private void validateDateRange(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            return; // Deixa as validações @NotNull dos DTOs cuidarem disso
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new InvalidDateRangeException("A data de fim do evento deve ser igual ou posterior à data de início");
        }
    }

    /**
     * Obtém o usuário autenticado do contexto de segurança
     */
    private UsuarioEntity getUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                throw new SecurityException("Nenhum usuário autenticado encontrado");
            }
            
            String email = authentication.getName();
            return usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("Usuário autenticado não encontrado: " + email));
        } catch (Exception e) {
            throw new SecurityException("Erro ao obter usuário autenticado: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateEventoOwnership(Long eventoId, Long usuarioId) {
        if (!isEventoOwner(eventoId, usuarioId)) {
            throw new IllegalArgumentException("Você não tem permissão para acessar este evento");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEventoOwner(Long eventoId, Long usuarioId) {
        if (eventoId == null || usuarioId == null) {
            return false;
        }

        try {
            EventoEntity evento = eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + eventoId));

            // Verifica se o evento tem organizador associado
            if (evento.getOrganizador() == null) {
                return false;
            }

            // Verifica se o usuário logado é o mesmo usuário organizador do evento
            return evento.getOrganizador().getId().equals(usuarioId);
        } catch (Exception e) {
            // Em caso de erro, considera que não é o owner
            return false;
        }
    }

    @Override
    @CachePut(value = "eventos-dto", key = "#eventoId")
    public EventoResponseDTO changeStatus(Long eventoId, StatusChangeDTO statusChangeDTO) {
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + eventoId));

        StatusEvento statusAtual = evento.getStatus();
        StatusEvento novoStatus = statusChangeDTO.getNovoStatus();

        // Validar transições permitidas
        validateStatusTransition(statusAtual, novoStatus);

        // Aplicar a mudança de status usando os métodos da entidade
        applyStatusChange(evento, novoStatus);

        // Salvar e retornar
        EventoEntity eventoSalvo = eventoRepository.save(evento);
        return convertToResponseDTO(eventoSalvo);
    }

    private void validateStatusTransition(StatusEvento statusAtual, StatusEvento novoStatus) {
        if (statusAtual == novoStatus) {
            throw new IllegalArgumentException("O evento já está no status informado");
        }

        // Regras de transição
        switch (statusAtual) {
            case RASCUNHO -> {
                if (novoStatus != StatusEvento.ABERTO && novoStatus != StatusEvento.CANCELADO) {
                    throw new IllegalArgumentException("De RASCUNHO só é possível ir para ABERTO ou CANCELADO");
                }
            }
            case ABERTO -> {
                if (novoStatus != StatusEvento.INSCRICOES_ENCERRADAS && 
                    novoStatus != StatusEvento.CANCELADO && 
                    novoStatus != StatusEvento.ADIADO) {
                    throw new IllegalArgumentException("De ABERTO só é possível ir para INSCRICOES_ENCERRADAS, CANCELADO ou ADIADO");
                }
            }
            case INSCRICOES_ENCERRADAS -> {
                if (novoStatus != StatusEvento.EM_ANDAMENTO && 
                    novoStatus != StatusEvento.CANCELADO && 
                    novoStatus != StatusEvento.ADIADO) {
                    throw new IllegalArgumentException("De INSCRICOES_ENCERRADAS só é possível ir para EM_ANDAMENTO, CANCELADO ou ADIADO");
                }
            }
            case EM_ANDAMENTO -> {
                if (novoStatus != StatusEvento.FINALIZADO && novoStatus != StatusEvento.CANCELADO) {
                    throw new IllegalArgumentException("De EM_ANDAMENTO só é possível ir para FINALIZADO ou CANCELADO");
                }
            }
            case FINALIZADO -> {
                throw new IllegalArgumentException("Não é possível alterar o status de um evento FINALIZADO");
            }
            case CANCELADO -> {
                if (novoStatus != StatusEvento.ADIADO && novoStatus != StatusEvento.ABERTO) {
                    throw new IllegalArgumentException("De CANCELADO só é possível ir para ADIADO ou ABERTO");
                }
            }
            case ADIADO -> {
                if (novoStatus != StatusEvento.ABERTO && novoStatus != StatusEvento.CANCELADO) {
                    throw new IllegalArgumentException("De ADIADO só é possível ir para ABERTO ou CANCELADO");
                }
            }
        }
    }

    private void applyStatusChange(EventoEntity evento, StatusEvento novoStatus) {
        switch (novoStatus) {
            case ABERTO -> evento.publicar();
            case INSCRICOES_ENCERRADAS -> evento.encerrarInscricoes();
            case EM_ANDAMENTO -> evento.iniciar();
            case FINALIZADO -> evento.finalizar();
            case CANCELADO -> evento.cancelar();
            case ADIADO -> evento.adiar();
            case RASCUNHO -> evento.setStatus(StatusEvento.RASCUNHO);
        }
    }
}
