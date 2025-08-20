package br.com.eventsports.minha_inscricao.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter Order Debug Configuration to understand the filter chain execution order.
 * This helps identify if filters are interfering with each other causing 403 errors.
 * 
 * Usage: Add -Dspring.profiles.active=filter-debug to JVM arguments
 */
@Configuration
@Profile("filter-debug")
public class FilterOrderDebugConfig {

    @Bean
    public FilterRegistrationBean<RequestDebugFilter> requestDebugFilter() {
        FilterRegistrationBean<RequestDebugFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestDebugFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // Execute first
        registration.setName("RequestDebugFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityDebugFilter> securityDebugFilter() {
        FilterRegistrationBean<SecurityDebugFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityDebugFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100); // Execute after request debug
        registration.setName("SecurityDebugFilter");
        return registration;
    }

    /**
     * Filter to log all incoming requests and their details
     */
    public static class RequestDebugFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            System.out.println("🔍 RequestDebugFilter initialized");
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, 
                           FilterChain chain) throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            long startTime = System.currentTimeMillis();
            String method = httpRequest.getMethod();
            String uri = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String fullURL = uri + (queryString != null ? "?" + queryString : "");
            
            System.out.println("\n🚀 REQUEST START: " + method + " " + fullURL);
            System.out.println("📍 Remote Address: " + httpRequest.getRemoteAddr());
            System.out.println("🌐 Origin: " + httpRequest.getHeader("Origin"));
            System.out.println("📋 Content-Type: " + httpRequest.getHeader("Content-Type"));
            System.out.println("🔑 Authorization: " + (httpRequest.getHeader("Authorization") != null ? "Present" : "None"));
            
            // Log all headers for debugging
            System.out.println("📨 Request Headers:");
            httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                System.out.println("  - " + headerName + ": " + httpRequest.getHeader(headerName));
            });
            
            try {
                // Continue the filter chain
                chain.doFilter(request, response);
                
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("✅ REQUEST COMPLETED: " + method + " " + fullURL);
                System.out.println("📊 Status: " + httpResponse.getStatus() + ", Duration: " + duration + "ms");
                
                // Log response headers if there was an error
                if (httpResponse.getStatus() >= 400) {
                    System.out.println("❌ ERROR RESPONSE HEADERS:");
                    httpResponse.getHeaderNames().forEach(headerName -> {
                        System.out.println("  - " + headerName + ": " + httpResponse.getHeader(headerName));
                    });
                }
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("💥 REQUEST FAILED: " + method + " " + fullURL);
                System.out.println("⚠️ Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                System.out.println("⏱️ Duration: " + duration + "ms");
                throw e;
            }
            
            System.out.println("🏁 REQUEST END: " + method + " " + fullURL + "\n");
        }

        @Override
        public void destroy() {
            System.out.println("🔍 RequestDebugFilter destroyed");
        }
    }

    /**
     * Filter to debug security-related processing
     */
    public static class SecurityDebugFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            System.out.println("🛡️ SecurityDebugFilter initialized");
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, 
                           FilterChain chain) throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String method = httpRequest.getMethod();
            String uri = httpRequest.getRequestURI();
            
            System.out.println("🛡️ SECURITY CHECK: " + method + " " + uri);
            
            // Check if this is a preflight request
            if ("OPTIONS".equalsIgnoreCase(method)) {
                System.out.println("✈️ Preflight OPTIONS request detected");
                String accessControlRequestMethod = httpRequest.getHeader("Access-Control-Request-Method");
                if (accessControlRequestMethod != null) {
                    System.out.println("🎯 Target method: " + accessControlRequestMethod);
                }
            }
            
            // Check authentication status
            if (httpRequest.getUserPrincipal() != null) {
                System.out.println("👤 Authenticated user: " + httpRequest.getUserPrincipal().getName());
            } else {
                System.out.println("👻 No authenticated user (anonymous)");
            }
            
            try {
                chain.doFilter(request, response);
                
                if (httpResponse.getStatus() == 403) {
                    System.out.println("🚫 403 FORBIDDEN detected for: " + method + " " + uri);
                    System.out.println("🔍 Possible causes:");
                    System.out.println("  - CORS preflight failure");
                    System.out.println("  - Security filter chain blocking request");
                    System.out.println("  - Missing or invalid authentication");
                    System.out.println("  - Method security annotations");
                }
                
            } catch (Exception e) {
                System.out.println("🛡️ Security filter exception: " + e.getMessage());
                throw e;
            }
        }

        @Override
        public void destroy() {
            System.out.println("🛡️ SecurityDebugFilter destroyed");
        }
    }
}