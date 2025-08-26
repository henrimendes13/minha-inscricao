package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.anexo.AnexoResponseDTO;
import br.com.eventsports.minha_inscricao.dto.anexo.AnexoSummaryDTO;
import br.com.eventsports.minha_inscricao.entity.AnexoEntity;
import br.com.eventsports.minha_inscricao.service.AnexoService.EstatisticasAnexo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IAnexoService {
    
    /**
     * Salva um novo anexo com upload de arquivo
     */
    AnexoEntity salvarAnexo(MultipartFile arquivo, Long eventoId, String descricao) throws IOException;
    
    /**
     * Busca anexos de um evento
     */
    List<AnexoEntity> buscarAnexosDoEvento(Long eventoId);
    
    /**
     * Busca anexo por ID
     */
    Optional<AnexoEntity> buscarPorId(Long id);
    
    /**
     * Baixa um arquivo anexo
     */
    Resource baixarArquivo(Long anexoId) throws IOException;
    
    /**
     * Atualiza descrição de um anexo
     */
    AnexoEntity atualizarDescricao(Long anexoId, String novaDescricao);
    
    /**
     * Ativa/Desativa um anexo
     */
    AnexoEntity alterarStatus(Long anexoId, boolean ativo);
    
    /**
     * Remove um anexo (soft delete)
     */
    void removerAnexo(Long anexoId);
    
    /**
     * Remove um anexo permanentemente (hard delete)
     */
    void removerAnexoPermanentemente(Long anexoId) throws IOException;
    
    /**
     * Busca anexos por tipo (imagens, documentos, etc.)
     */
    List<AnexoEntity> buscarPorTipo(String tipoBase);
    
    /**
     * Obtém estatísticas de anexos de um evento
     */
    EstatisticasAnexo obterEstatisticas(Long eventoId);
    
    /**
     * Converte uma entidade para DTO de resposta
     */
    AnexoResponseDTO toResponseDTO(AnexoEntity anexo);
    
    /**
     * Converte uma entidade para DTO de resumo
     */
    AnexoSummaryDTO toSummaryDTO(AnexoEntity anexo);
}
