package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.usuario.*;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IUsuarioService {
    
    UsuarioResponseDTO criar(UsuarioCreateDTO dto);
    
    UsuarioComOrganizadorResponseDTO criarComOrganizador(UsuarioComOrganizadorCreateDTO dto);
    
    UsuarioResponseDTO buscarPorId(Long id);
    
    UsuarioResponseDTO buscarPorEmail(String email);
    
    Page<UsuarioSummaryDTO> listarAtivos(Pageable pageable);
    
    List<UsuarioSummaryDTO> listarPorTipo(TipoUsuario tipo);
    
    Page<UsuarioSummaryDTO> buscarPorNome(String nome, Pageable pageable);
    
    UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto);
    
    void desativar(Long id);
    
    void ativar(Long id);
    
    void registrarLogin(Long id);
    
    boolean validarCredenciais(String email, String senha);
    
    Object obterEstatisticas();
    
    boolean organizadorTemPerfilCompleto(Long usuarioId);
    
    UsuarioComOrganizadorResponseDTO buscarComOrganizador(Long usuarioId);
}
