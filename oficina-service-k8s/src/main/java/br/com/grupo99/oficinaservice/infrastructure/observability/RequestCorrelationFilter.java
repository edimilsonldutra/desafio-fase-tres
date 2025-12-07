package br.com.grupo99.oficinaservice.infrastructure.observability;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para adicionar informações de correlação em cada requisição.
 * Implementa logs estruturados com traceId e spanId para rastreamento distribuído.
 */
@Component
public class RequestCorrelationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestCorrelationFilter.class);
    
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    
    private static final String TRACE_ID_MDC = "traceId";
    private static final String SPAN_ID_MDC = "spanId";
    private static final String REQUEST_ID_MDC = "requestId";
    private static final String USER_ID_MDC = "userId";
    private static final String REQUEST_URI_MDC = "requestUri";
    private static final String REQUEST_METHOD_MDC = "requestMethod";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Obtém ou cria IDs de correlação
            String traceId = getOrCreateHeader(httpRequest, TRACE_ID_HEADER);
            String spanId = UUID.randomUUID().toString();
            String requestId = getOrCreateHeader(httpRequest, REQUEST_ID_HEADER);
            
            // Adiciona ao MDC para logs estruturados
            MDC.put(TRACE_ID_MDC, traceId);
            MDC.put(SPAN_ID_MDC, spanId);
            MDC.put(REQUEST_ID_MDC, requestId);
            MDC.put(REQUEST_URI_MDC, httpRequest.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC, httpRequest.getMethod());
            
            // Adiciona aos headers de resposta
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);
            httpResponse.setHeader(SPAN_ID_HEADER, spanId);
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
            
            logger.info("Request iniciada: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            
            chain.doFilter(request, response);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request finalizada: {} {} - Status: {} - Duração: {}ms",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration);
                    
        } finally {
            // Limpa o MDC
            MDC.clear();
        }
    }
    
    private String getOrCreateHeader(HttpServletRequest request, String headerName) {
        String header = request.getHeader(headerName);
        return header != null ? header : UUID.randomUUID().toString();
    }
}
