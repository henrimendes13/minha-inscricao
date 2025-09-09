package br.com.eventsports.minha_inscricao.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.eventsports.minha_inscricao.config.ImagemConfig;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImagemService {

    private final ImagemConfig imagemConfig;
    private final EventoRepository eventoRepository;

    /**
     * Faz upload da imagem do evento
     */
    @CacheEvict(value = "eventos-dto", key = "#eventoId")
    public String uploadImagemEvento(MultipartFile arquivo, Long eventoId) throws IOException {
        log.info("Iniciando upload de imagem para evento ID: {}", eventoId);

        // Validações
        validarImagem(arquivo);
        EventoEntity evento = validarEvento(eventoId);

        // Preparar diretório
        Path diretorio = Paths.get(imagemConfig.getDiretorioUpload()).toAbsolutePath().normalize();
        Files.createDirectories(diretorio);

        // Gerar nome único para o arquivo
        String extensao = obterExtensao(arquivo.getOriginalFilename());
        String nomeArquivo = String.format("%s_%d_%s.%s", 
            imagemConfig.getPrefixoNomeArquivo(), 
            eventoId,
            UUID.randomUUID().toString().substring(0, 8),
            extensao);

        // Caminho completo do arquivo
        Path caminhoArquivo = diretorio.resolve(nomeArquivo);
        
        // Salvar arquivo
        Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        // URL relativa para salvar no banco
        String imagemUrl = imagemConfig.getUrlBase() + nomeArquivo;

        // Remover imagem anterior se existir
        if (evento.getImagemUrl() != null && !evento.getImagemUrl().isEmpty()) {
            removerImagemAnterior(evento.getImagemUrl());
        }

        // Atualizar evento com nova URL da imagem
        evento.setImagemUrl(imagemUrl);
        eventoRepository.save(evento);

        log.info("Imagem salva com sucesso: {}", nomeArquivo);
        return imagemUrl;
    }

    /**
     * Remove a imagem do evento
     */
    @CacheEvict(value = "eventos-dto", key = "#eventoId")
    public void removerImagemEvento(Long eventoId) {
        log.info("Removendo imagem do evento ID: {}", eventoId);
        
        EventoEntity evento = validarEvento(eventoId);
        
        if (evento.getImagemUrl() != null && !evento.getImagemUrl().isEmpty()) {
            removerImagemAnterior(evento.getImagemUrl());
            evento.setImagemUrl(null);
            eventoRepository.save(evento);
            log.info("Imagem removida com sucesso do evento ID: {}", eventoId);
        }
    }

    /**
     * Carrega uma imagem como Resource
     */
    public Resource carregarImagem(String nomeArquivo) throws IOException {
        Path caminhoArquivo = Paths.get(imagemConfig.getDiretorioUpload())
                .toAbsolutePath()
                .normalize()
                .resolve(nomeArquivo);

        Resource resource = new UrlResource(caminhoArquivo.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Arquivo não encontrado: " + nomeArquivo);
        }
    }

    /**
     * Valida se o arquivo é uma imagem válida
     */
    private void validarImagem(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo foi fornecido");
        }

        if (arquivo.getSize() > imagemConfig.getTamanhoMaximoBytes()) {
            throw new IllegalArgumentException(
                String.format("Arquivo muito grande. Tamanho máximo: %s", 
                    imagemConfig.getTamanhoMaximoFormatado()));
        }

        String tipoMime = arquivo.getContentType();
        if (tipoMime == null || !imagemConfig.getTiposPermitidos().contains(tipoMime.toLowerCase())) {
            throw new IllegalArgumentException(
                String.format("Tipo de arquivo não permitido. Tipos aceitos: %s", 
                    String.join(", ", imagemConfig.getTiposPermitidos())));
        }

        String extensao = obterExtensao(arquivo.getOriginalFilename());
        if (!imagemConfig.getExtensoesPermitidas().contains(extensao.toLowerCase())) {
            throw new IllegalArgumentException(
                String.format("Extensão de arquivo não permitida. Extensões aceitas: %s", 
                    String.join(", ", imagemConfig.getExtensoesPermitidas())));
        }
    }

    /**
     * Valida se o evento existe
     */
    private EventoEntity validarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com ID: " + eventoId));
    }

    /**
     * Remove imagem anterior do disco
     */
    private void removerImagemAnterior(String imagemUrl) {
        try {
            if (imagemUrl.startsWith(imagemConfig.getUrlBase())) {
                String nomeArquivo = imagemUrl.substring(imagemConfig.getUrlBase().length());
                Path caminhoArquivo = Paths.get(imagemConfig.getDiretorioUpload())
                        .toAbsolutePath()
                        .normalize()
                        .resolve(nomeArquivo);
                
                if (Files.exists(caminhoArquivo)) {
                    Files.delete(caminhoArquivo);
                    log.info("Imagem anterior removida: {}", nomeArquivo);
                }
            }
        } catch (IOException e) {
            log.warn("Erro ao remover imagem anterior: {}", e.getMessage());
        }
    }

    /**
     * Extrai a extensão do nome do arquivo
     */
    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.isEmpty()) {
            return "";
        }
        
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto == -1) {
            return "";
        }
        
        return nomeArquivo.substring(ultimoPonto + 1).toLowerCase();
    }

    /**
     * Verifica se o arquivo é uma imagem
     */
    public boolean isImagem(String tipoMime) {
        return tipoMime != null && imagemConfig.getTiposPermitidos().contains(tipoMime.toLowerCase());
    }
}