package br.com.eventsports.minha_inscricao.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller de debug para testar configurações de segurança
 * REMOVER EM PRODUÇÃO
 */
@RestController
@RequestMapping("/api/debug")
@Slf4j
public class DebugController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint(HttpServletRequest request) {
        log.info("🧪 DEBUG - Endpoint público acessado");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint público funcionando!");
        response.put("timestamp", LocalDateTime.now());
        response.put("method", request.getMethod());
        response.put("uri", request.getRequestURI());
        
        response.put("authentication", "Security removed");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/public")
    public ResponseEntity<Map<String, Object>> publicPostEndpoint(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        log.info("🧪 DEBUG - Endpoint POST público acessado com body: {}", body);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "POST público funcionando!");
        response.put("timestamp", LocalDateTime.now());
        response.put("method", request.getMethod());
        response.put("uri", request.getRequestURI());
        response.put("receivedBody", body);
        
        response.put("authentication", "Security removed");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/security-context")
    public ResponseEntity<Map<String, Object>> securityContext() {
        log.info("🧪 DEBUG - Verificando contexto de segurança");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        response.put("status", "SECURITY_REMOVED");
        response.put("authentication", "Security system has been removed");
        
        return ResponseEntity.ok(response);
    }
}