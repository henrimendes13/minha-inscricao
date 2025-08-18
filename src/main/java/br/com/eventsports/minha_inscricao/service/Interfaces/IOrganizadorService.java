package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.organizador.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrganizadorService {
    
    OrganizadorResponseDTO criar(OrganizadorCreateDTO dto);
    
    OrganizadorResponseDTO buscarPorId(Long id);
    
    OrganizadorResponseDTO buscarPorUsuarioId(Long usuarioId);
    
    OrganizadorResponseDTO buscarPorCnpj(String cnpj);
    
    Page<OrganizadorSummaryDTO> listarTodos(Pageable pageable);
    
    Page<OrganizadorSummaryDTO> listarVerificados(Pageable pageable);
    
    Page<OrganizadorSummaryDTO> listarQuePodemOrganizarEventos(Pageable pageable);
    
    Page<OrganizadorSummaryDTO> buscarPorNomeEmpresa(String nomeEmpresa, Pageable pageable);
    
    Page<OrganizadorSummaryDTO> buscarVerificadosPorNomeEmpresa(String nomeEmpresa, Pageable pageable);
    
    OrganizadorResponseDTO atualizar(Long id, OrganizadorUpdateDTO dto);
    
    void verificar(Long id);
    
    void removerVerificacao(Long id);
    
    List<OrganizadorSummaryDTO> listarPorCidade(String cidade);
    
    List<OrganizadorSummaryDTO> listarComEventos();
    
    Object obterEstatisticas();
    
    boolean podeOrganizarEventos(Long id);
}
