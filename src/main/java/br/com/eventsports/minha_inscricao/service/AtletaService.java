package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.atleta.*;
import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.repository.AtletaRepository;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.EquipeRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAtletaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Cacheable(value = "atletas", key = "#id")
    @Transactional(readOnly = true)
    public AtletaResponseDTO findById(Long id) {
        AtletaEntity atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado com ID: " + id));
        return convertToResponseDTO(atleta);
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

    @CachePut(value = "atletas", key = "#result.id")
    @CacheEvict(value = "atletas", key = "'all'")
    public AtletaResponseDTO saveForEvento(AtletaCreateDTO atletaCreateDTO, Long eventoId) {
        validateAtletaData(atletaCreateDTO);
        AtletaEntity atleta = convertCreateDTOToEntityForEvento(atletaCreateDTO, eventoId);
        AtletaEntity savedAtleta = atletaRepository.save(atleta);
        return convertToResponseDTO(savedAtleta);
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
}
