package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.evento.*;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.OrganizadorEntity;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Cacheable(value = "eventos", key = "#id")
    @Transactional(readOnly = true)
    public EventoResponseDTO findById(Long id) {
        EventoEntity evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + id));
        return convertToResponseDTO(evento);
    }

    @Cacheable(value = "eventos", key = "'all'")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findAll() {
        List<EventoEntity> eventos = eventoRepository.findAll();
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "eventos", key = "#result.id")
    @CacheEvict(value = "eventos", key = "'all'")
    public EventoResponseDTO save(EventoCreateDTO eventoCreateDTO) {
        EventoEntity evento = convertCreateDTOToEntity(eventoCreateDTO);
        EventoEntity savedEvento = eventoRepository.save(evento);
        return convertToResponseDTO(savedEvento);
    }

    @CachePut(value = "eventos", key = "#id")
    @CacheEvict(value = "eventos", key = "'all'")
    public EventoResponseDTO update(Long id, EventoUpdateDTO eventoUpdateDTO) {
        EventoEntity existingEvento = eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + id));

        updateEventoFromUpdateDTO(existingEvento, eventoUpdateDTO);
        EventoEntity updatedEvento = eventoRepository.save(existingEvento);
        return convertToResponseDTO(updatedEvento);
    }

    @CacheEvict(value = "eventos", allEntries = true)
    public void deleteById(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new EventoNotFoundException("Evento não encontrado com ID: " + id);
        }
        eventoRepository.deleteById(id);
    }

    @Cacheable(value = "eventos", key = "'search:' + #nome")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findByNome(String nome) {
        List<EventoEntity> eventos = eventoRepository.findByNomeContainingIgnoreCase(nome);
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "eventos", key = "'upcoming'")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findEventosUpcoming() {
        List<EventoEntity> eventos = eventoRepository.findEventosUpcoming();
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "eventos", key = "'past'")
    @Transactional(readOnly = true)
    public List<EventoSummaryDTO> findEventosPast() {
        List<EventoEntity> eventos = eventoRepository.findEventosPast();
        return eventos.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "eventos", key = "'between:' + #inicio + ':' + #fim")
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
                .data(evento.getData())
                .status(evento.getStatus() != null ? evento.getStatus().name() : null)
                .descricaoStatus(evento.getDescricaoStatus())
                .nomeOrganizador(evento.getNomeOrganizador())
                .descricao(evento.getDescricao())
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
                .data(evento.getData())
                .status(evento.getStatus() != null ? evento.getStatus().name() : null)
                .descricaoStatus(evento.getDescricaoStatus())
                .nomeOrganizador(evento.getNomeOrganizador())
                .totalCategorias(evento.getTotalCategorias())
                .inscricoesAtivas(evento.getInscricoesAtivas())
                .podeReceberInscricoes(evento.podeReceberInscricoes())
                .createdAt(evento.getCreatedAt())
                .build();
    }

    private EventoEntity convertCreateDTOToEntity(EventoCreateDTO dto) {
        EventoEntity.EventoEntityBuilder builder = EventoEntity.builder()
                .nome(dto.getNome())
                .data(dto.getData())
                .descricao(dto.getDescricao());

        // TODO: Implementar lógica para obter o organizador do usuário autenticado
        // Por enquanto, usa o organizador padrão (ID = 1) até implementar autenticação
        OrganizadorEntity organizadorPadrao = getOrganizadorPadrao();
        if (organizadorPadrao != null) {
            builder.organizador(organizadorPadrao);
        }

        return builder.build();
    }

    private void updateEventoFromUpdateDTO(EventoEntity evento, EventoUpdateDTO dto) {
        evento.setNome(dto.getNome());
        evento.setData(dto.getData());
        evento.setDescricao(dto.getDescricao());

        // Update organizador if provided
        if (dto.getOrganizadorId() != null) {
            OrganizadorEntity organizador = new OrganizadorEntity();
            organizador.setId(dto.getOrganizadorId());
            evento.setOrganizador(organizador);
        }
    }

    /**
     * Método temporário para obter um organizador padrão.
     * TODO: Substituir pela lógica de autenticação quando implementada.
     * Deve retornar o organizador baseado no usuário logado.
     */
    private OrganizadorEntity getOrganizadorPadrao() {
        try {
            // Por enquanto, retorna um organizador com ID = 1 (se existir)
            // Em produção, isso deve ser obtido do contexto de segurança
            OrganizadorEntity organizador = new OrganizadorEntity();
            organizador.setId(1L); // ID padrão
            return organizador;
        } catch (Exception e) {
            // Se não conseguir obter o organizador, retorna null
            // O evento será criado sem organizador definido
            return null;
        }
    }
}
