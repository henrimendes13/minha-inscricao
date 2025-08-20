package br.com.eventsports.minha_inscricao.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            response.put("authentication", Map.of(
                "class", auth.getClass().getSimpleName(),
                "principal", auth.getPrincipal().toString(),
                "authorities", auth.getAuthorities().toString(),
                "authenticated", auth.isAuthenticated()
            ));
        } else {
            response.put("authentication", "null");
        }
        
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
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            response.put("authentication", Map.of(
                "class", auth.getClass().getSimpleName(),
                "principal", auth.getPrincipal().toString(),
                "authorities", auth.getAuthorities().toString(),
                "authenticated", auth.isAuthenticated()
            ));
        } else {
            response.put("authentication", "null");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/security-context")
    public ResponseEntity<Map<String, Object>> securityContext() {
        log.info("🧪 DEBUG - Verificando contexto de segurança");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            response.put("status", "NO_AUTHENTICATION");
            response.put("authentication", null);
        } else {
            response.put("status", "AUTHENTICATED");
            response.put("authentication", Map.of(
                "class", auth.getClass().getSimpleName(),
                "name", auth.getName(),
                "principal", auth.getPrincipal().toString(),
                "authorities", auth.getAuthorities().toString(),
                "authenticated", auth.isAuthenticated(),
                "details", auth.getDetails() != null ? auth.getDetails().toString() : "null"
            ));
        }
        
        return ResponseEntity.ok(response);
    }
}