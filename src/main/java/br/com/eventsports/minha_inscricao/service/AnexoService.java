package br.com.eventsports.minha_inscricao.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.eventsports.minha_inscricao.config.ArquivoConfig;
import br.com.eventsports.minha_inscricao.dto.anexo.AnexoResponseDTO;
import br.com.eventsports.minha_inscricao.dto.anexo.AnexoSummaryDTO;
import br.com.eventsports.minha_inscricao.entity.AnexoEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.repository.AnexoRepository;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IAnexoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnexoService implements IAnexoService {

    private final AnexoRepository anexoRepository;
    private final EventoRepository eventoRepository;
    private final ArquivoConfig arquivoConfig;

    /**
     * Salva um novo anexo com upload de arquivo
     */
    public AnexoEntity salvarAnexo(MultipartFile arquivo, Long eventoId, String descricao) throws IOException {
        log.info("Iniciando upload de anexo para evento ID: {}", eventoId);

        // Validações
        validarArquivo(arquivo);
        validarEvento(eventoId);

        // Criar diretório se não existir
        Path diretorio = criarDiretorioSeNecessario();

        // Gerar nome único para o arquivo
        String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
        Path caminhoCompleto = diretorio.resolve(nomeUnico);

        // CORREÇÃO: Ler bytes UMA única vez para evitar corrupção
        byte[] arquivoBytes = arquivo.getBytes();
        
        // Calcular MD5 dos bytes lidos
        String md5 = calcularMd5(arquivoBytes);

        // Verificar se já existe arquivo com mesmo MD5
        Optional<AnexoEntity> anexoExistente = anexoRepository.findByChecksumMd5(md5);
        if (anexoExistente.isPresent()) {
            log.warn("Arquivo com MD5 {} já existe: {}", md5, anexoExistente.get().getNomeArquivo());
            throw new IllegalArgumentException("Arquivo já existe no sistema");
        }

        // Salvar bytes diretamente no arquivo (não usar InputStream já consumido)
        Files.write(caminhoCompleto, arquivoBytes, StandardOpenOption.CREATE);
        log.info("Arquivo salvo em: {} ({} bytes)", caminhoCompleto, arquivoBytes.length);

        // Criar entidade
        AnexoEntity anexo = AnexoEntity.builder()
                .nomeArquivo(arquivo.getOriginalFilename())
                .descricao(descricao)
                .caminhoArquivo(caminhoCompleto.toString())
                .tipoMime(arquivo.getContentType())
                .tamanhoBytes(arquivo.getSize())
                .checksumMd5(md5)
                .evento(EventoEntity.builder().id(eventoId).build())
                .build();

        AnexoEntity anexoSalvo = anexoRepository.save(anexo);
        log.info("Anexo criado com ID: {}", anexoSalvo.getId());

        return anexoSalvo;
    }

    /**
     * Busca anexos de um evento
     */
    @Transactional(readOnly = true)
    public List<AnexoEntity> buscarAnexosDoEvento(Long eventoId) {
        return anexoRepository.findByEventoIdAndAtivoTrue(eventoId);
    }

    /**
     * Busca anexo por ID
     */
    @Transactional(readOnly = true)
    public Optional<AnexoEntity> buscarPorId(Long id) {
        return anexoRepository.findById(id);
    }

    /**
     * Baixa um arquivo anexo
     */
    @Transactional(readOnly = true)
    public Resource baixarArquivo(Long anexoId) throws IOException {
        AnexoEntity anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));

        if (!anexo.isAtivo()) {
            throw new IllegalArgumentException("Anexo está inativo");
        }

        Path caminhoArquivo = Paths.get(anexo.getCaminhoArquivo());
        if (!Files.exists(caminhoArquivo)) {
            log.error("Arquivo físico não encontrado: {}", caminhoArquivo);
            throw new IOException("Arquivo não encontrado no sistema de arquivos");
        }

        Resource resource = new UrlResource(caminhoArquivo.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Não foi possível ler o arquivo");
        }
    }

    /**
     * Atualiza descrição de um anexo
     */
    public AnexoEntity atualizarDescricao(Long anexoId, String novaDescricao) {
        AnexoEntity anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));

        anexo.setDescricao(novaDescricao);
        return anexoRepository.save(anexo);
    }

    /**
     * Ativa/Desativa um anexo
     */
    public AnexoEntity alterarStatus(Long anexoId, boolean ativo) {
        AnexoEntity anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));

        if (ativo) {
            anexo.ativar();
        } else {
            anexo.desativar();
        }

        return anexoRepository.save(anexo);
    }

    /**
     * Remove um anexo definitivamente (hard delete)
     */
    public void removerAnexo(Long anexoId) {
        try {
            removerAnexoPermanentemente(anexoId);
        } catch (IOException e) {
            log.error("Erro ao remover anexo {}", anexoId, e);
            throw new RuntimeException("Erro ao remover anexo: " + e.getMessage(), e);
        }
    }

    /**
     * Remove um anexo permanentemente (hard delete)
     */
    public void removerAnexoPermanentemente(Long anexoId) throws IOException {
        AnexoEntity anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));

        // Remover arquivo físico
        Path caminhoArquivo = Paths.get(anexo.getCaminhoArquivo());
        if (Files.exists(caminhoArquivo)) {
            Files.delete(caminhoArquivo);
            log.info("Arquivo físico removido: {}", caminhoArquivo);
        }

        // Remover do banco
        anexoRepository.delete(anexo);
        log.info("Anexo removido permanentemente: {}", anexoId);
    }

    /**
     * Busca anexos por tipo (imagens, documentos, etc.)
     */
    @Transactional(readOnly = true)
    public List<AnexoEntity> buscarPorTipo(String tipoBase) {
        return anexoRepository.findByTipoBase(tipoBase);
    }

    /**
     * Obtém estatísticas de anexos de um evento
     */
    @Transactional(readOnly = true)
    public EstatisticasAnexo obterEstatisticas(Long eventoId) {
        long quantidade = anexoRepository.countByEventoIdAndAtivoTrue(eventoId);
        Long tamanhoTotal = anexoRepository.sumTamanhoByEventoId(eventoId);

        return EstatisticasAnexo.builder()
                .quantidadeAnexos(quantidade)
                .tamanhoTotalBytes(tamanhoTotal != null ? tamanhoTotal : 0L)
                .tamanhoTotalFormatado(formatarTamanho(tamanhoTotal != null ? tamanhoTotal : 0L))
                .build();
    }

    // Métodos privados auxiliares

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }

        if (arquivo.getSize() > arquivoConfig.getTamanhoMaximoBytes()) {
            throw new IllegalArgumentException("Arquivo muito grande. Máximo permitido: " +
                    arquivoConfig.getTamanhoMaximoFormatado());
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !arquivoConfig.getTiposPermitidos().contains(contentType)) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido: " + contentType);
        }

        // Validação adicional por extensão
        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo != null) {
            String extensao = extrairExtensao(nomeArquivo);
            if (!arquivoConfig.getExtensoesPermitidas().contains(extensao)) {
                throw new IllegalArgumentException("Extensão de arquivo não permitida: " + extensao);
            }
        }
    }

    private void validarEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new IllegalArgumentException("Evento não encontrado: " + eventoId);
        }
    }

    private Path criarDiretorioSeNecessario() throws IOException {
        Path diretorio = Paths.get(arquivoConfig.getDiretorioUpload());
        if (!Files.exists(diretorio)) {
            Files.createDirectories(diretorio);
            log.info("Diretório criado: {}", diretorio);
        }
        return diretorio;
    }

    private String gerarNomeUnico(String nomeOriginal) {
        String extensao = extrairExtensao(nomeOriginal);
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (arquivoConfig.isManterNomeOriginal() && nomeOriginal != null) {
            String nomeBase = nomeOriginal.substring(0, nomeOriginal.lastIndexOf('.'));
            return String.format("%s_%s_%s.%s", nomeBase, timestamp, uuid.substring(0, 8), extensao);
        } else {
            return String.format("%s_%s_%s.%s",
                    arquivoConfig.getPrefixoNomeArquivo(), timestamp, uuid.substring(0, 8), extensao);
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains("."))
            return "";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase();
    }

    private String calcularMd5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Erro ao calcular MD5", e);
            throw new RuntimeException("Erro interno ao processar arquivo", e);
        }
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // Métodos de mapeamento para DTOs
    public AnexoResponseDTO toResponseDTO(AnexoEntity anexo) {
        return AnexoResponseDTO.builder()
                .id(anexo.getId())
                .nomeArquivo(anexo.getNomeArquivo())
                .descricao(anexo.getDescricao())
                .caminhoArquivo(anexo.getCaminhoArquivo())
                .tipoMime(anexo.getTipoMime())
                .tamanhoBytes(anexo.getTamanhoBytes())
                .tamanhoFormatado(formatarTamanho(anexo.getTamanhoBytes()))
                .extensao(anexo.getExtensao())
                .checksumMd5(anexo.getChecksumMd5())
                .ativo(anexo.getAtivo())
                .eventoId(anexo.getEvento() != null ? anexo.getEvento().getId() : null)
                .nomeEvento(anexo.getEvento() != null ? anexo.getEvento().getNome() : null)
                .isImagem(isImagem(anexo.getTipoMime()))
                .isPdf(isPdf(anexo.getTipoMime()))
                .createdAt(anexo.getCreatedAt())
                .updatedAt(anexo.getUpdatedAt())
                .build();
    }

    public AnexoSummaryDTO toSummaryDTO(AnexoEntity anexo) {
        return AnexoSummaryDTO.builder()
                .id(anexo.getId())
                .nomeArquivo(anexo.getNomeArquivo())
                .descricao(anexo.getDescricao())
                .extensao(anexo.getExtensao())
                .tamanhoFormatado(formatarTamanho(anexo.getTamanhoBytes()))
                .ativo(anexo.getAtivo())
                .tipoMime(anexo.getTipoMime())
                .build();
    }

    private Boolean isImagem(String tipoMime) {
        return tipoMime != null && tipoMime.startsWith("image/");
    }

    private Boolean isPdf(String tipoMime) {
        return "application/pdf".equals(tipoMime);
    }

    // Classe interna para estatísticas
    @lombok.Data
    @lombok.Builder
    public static class EstatisticasAnexo {
        private long quantidadeAnexos;
        private long tamanhoTotalBytes;
        private String tamanhoTotalFormatado;
    }
}
