package br.com.eventsports.minha_inscricao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.imagem")
@Data
public class ImagemConfig {
    
    /**
     * Diretório onde as imagens dos eventos serão salvas
     */
    private String diretorioUpload = "./uploads/eventos/imagens/";
    
    /**
     * Tamanho máximo permitido para upload de imagens em bytes (padrão: 5MB)
     */
    private long tamanhoMaximoBytes = 5 * 1024 * 1024; // 5MB
    
    /**
     * Tipos MIME permitidos para upload de imagens
     */
    private List<String> tiposPermitidos = Arrays.asList(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/webp"
    );
    
    /**
     * Extensões permitidas (validação adicional)
     */
    private List<String> extensoesPermitidas = Arrays.asList(
        "jpg", "jpeg", "png", "webp"
    );
    
    /**
     * Prefixo para nomes de arquivos únicos
     */
    private String prefixoNomeArquivo = "evento";
    
    /**
     * Largura máxima para redimensionamento automático (pixels)
     */
    private int larguraMaxima = 1200;
    
    /**
     * Altura máxima para redimensionamento automático (pixels)
     */
    private int alturaMaxima = 800;
    
    /**
     * Qualidade de compressão JPEG (0-100)
     */
    private int qualidadeJpeg = 85;
    
    /**
     * Se deve manter proporções ao redimensionar
     */
    private boolean manterProporcoes = true;
    
    /**
     * URL base para servir as imagens
     */
    private String urlBase = "/api/eventos/imagens/";
    
    /**
     * Retorna o tamanho máximo formatado
     */
    public String getTamanhoMaximoFormatado() {
        if (tamanhoMaximoBytes < 1024) return tamanhoMaximoBytes + " B";
        if (tamanhoMaximoBytes < 1024 * 1024) return String.format("%.1f KB", tamanhoMaximoBytes / 1024.0);
        if (tamanhoMaximoBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", tamanhoMaximoBytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", tamanhoMaximoBytes / (1024.0 * 1024.0 * 1024.0));
    }
}