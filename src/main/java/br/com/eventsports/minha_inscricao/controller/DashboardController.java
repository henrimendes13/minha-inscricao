package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.dashboard.DashboardEstatisticasDTO;
import br.com.eventsports.minha_inscricao.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Endpoints para dashboard administrativo (requer permissão ADMIN)")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
        summary = "Obter estatísticas do dashboard",
        description = "Retorna estatísticas consolidadas de eventos, inscrições, usuários e financeiro. **ACESSO RESTRITO: Apenas admin@admin.com**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou não fornecido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas admin@admin.com")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("authentication.name == 'admin@admin.com'")
    @GetMapping("/estatisticas")
    public ResponseEntity<DashboardEstatisticasDTO> obterEstatisticas() {
        log.info("Requisição recebida para obter estatísticas do dashboard");

        DashboardEstatisticasDTO estatisticas = dashboardService.obterEstatisticas();

        log.info("Estatísticas obtidas com sucesso: {} eventos, {} inscrições, {} usuários",
                estatisticas.getEventos().getTotal(),
                estatisticas.getInscricoes().getTotal(),
                estatisticas.getUsuarios().getTotal());

        return ResponseEntity.ok(estatisticas);
    }
}
