package br.com.eventsports.minha_inscricao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.arquivo")
@Data
public class ArquivoConfig {
    
    /**
     * Diretório onde os arquivos serão salvos
     */
    private String diretorioUpload = "./uploads/anexos/";
    
    /**
     * Tamanho máximo permitido para upload em bytes (padrão: 10MB)
     */
    private long tamanhoMaximoBytes = 10 * 1024 * 1024; // 10MB
    
    /**
     * Tipos MIME permitidos para upload
     */
    private List<String> tiposPermitidos = Arrays.asList(
        "application/pdf",
        "image/jpeg", 
        "image/png",
        "image/gif",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
        "application/msword", // .doc
        "application/vnd.ms-excel", // .xls
        "text/plain", // .txt
        "application/zip",
        "application/x-rar-compressed"
    );
    
    /**
     * Extensões permitidas (validação adicional)
     */
    private List<String> extensoesPermitidas = Arrays.asList(
        "pdf", "jpg", "jpeg", "png", "gif", "docx", "doc", 
        "xlsx", "xls", "pptx", "txt", "zip", "rar"
    );
    
    /**
     * Prefixo para nomes de arquivos únicos
     */
    private String prefixoNomeArquivo = "anexo";
    
    /**
     * Se deve manter o nome original do arquivo (adiciona timestamp para uniqueness)
     */
    private boolean manterNomeOriginal = false;
    
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
