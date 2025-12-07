package br.com.grupo99.oficinaservice.infrastructure.observability;

import com.newrelic.api.agent.NewRelic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aspect para monitoramento de Ordens de Serviço.
 * Captura métricas customizadas e eventos de negócio para o New Relic.
 */
@Aspect
@Component
public class OrdemServicoMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(OrdemServicoMonitoringAspect.class);
    private static final Logger metricsLogger = LoggerFactory.getLogger("br.com.grupo99.oficinaservice.metrics");

    @Around("execution(* br.com.grupo99.oficinaservice.application.usecase.CriarOrdemServicoUseCase.execute(..))")
    public Object monitorCriacaoOrdemServico(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String operationId = UUID.randomUUID().toString();
        
        try {
            logger.info("Iniciando criação de ordem de serviço - operationId: {}", operationId);
            
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Registra métrica customizada no New Relic
            NewRelic.recordMetric("Custom/OrdemServico/Criacao/Duration", duration);
            NewRelic.incrementCounter("Custom/OrdemServico/Criacao/Count");
            
            // Log estruturado para métricas
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("event", "ordem_servico_criada");
            metrics.put("operationId", operationId);
            metrics.put("duration_ms", duration);
            metrics.put("status", "success");
            metricsLogger.info("Ordem de serviço criada: {}", metrics);
            
            logger.info("Ordem de serviço criada com sucesso - operationId: {} - duração: {}ms", 
                    operationId, duration);
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Registra erro no New Relic
            NewRelic.noticeError(e);
            NewRelic.incrementCounter("Custom/OrdemServico/Criacao/Errors");
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("event", "ordem_servico_erro");
            metrics.put("operationId", operationId);
            metrics.put("duration_ms", duration);
            metrics.put("status", "error");
            metrics.put("error", e.getMessage());
            metricsLogger.error("Erro ao criar ordem de serviço: {}", metrics);
            
            logger.error("Erro ao criar ordem de serviço - operationId: {}", operationId, e);
            throw e;
        }
    }

    @Around("execution(* br.com.grupo99.oficinaservice.application.usecase.AtualizarStatusOrdemServicoUseCase.execute(..))")
    public Object monitorAtualizacaoStatus(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Extrai informações dos argumentos
            UUID ordemServicoId = args.length > 0 ? (UUID) args[0] : null;
            String novoStatus = args.length > 1 ? args[1].toString() : "UNKNOWN";
            
            // Adiciona ao MDC para correlação
            if (ordemServicoId != null) {
                MDC.put("ordemServicoId", ordemServicoId.toString());
            }
            
            // Métricas por status
            NewRelic.recordMetric("Custom/OrdemServico/AtualizacaoStatus/" + novoStatus + "/Duration", duration);
            NewRelic.incrementCounter("Custom/OrdemServico/AtualizacaoStatus/" + novoStatus + "/Count");
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("event", "status_atualizado");
            metrics.put("ordemServicoId", ordemServicoId);
            metrics.put("novoStatus", novoStatus);
            metrics.put("duration_ms", duration);
            metricsLogger.info("Status atualizado: {}", metrics);
            
            logger.info("Status da ordem de serviço atualizado - ID: {} - Novo Status: {} - duração: {}ms",
                    ordemServicoId, novoStatus, duration);
            
            return result;
            
        } catch (Exception e) {
            NewRelic.noticeError(e);
            NewRelic.incrementCounter("Custom/OrdemServico/AtualizacaoStatus/Errors");
            
            logger.error("Erro ao atualizar status da ordem de serviço", e);
            throw e;
        }
    }

    @Around("execution(* br.com.grupo99.oficinaservice.infrastructure.integration..*(..))")
    public Object monitorIntegracoes(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        try {
            logger.info("Iniciando integração externa - {}.{}", className, methodName);
            
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            
            NewRelic.recordMetric("Custom/Integracao/" + className + "/" + methodName + "/Duration", duration);
            NewRelic.incrementCounter("Custom/Integracao/" + className + "/Success");
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("event", "integracao_externa");
            metrics.put("integration", className + "." + methodName);
            metrics.put("duration_ms", duration);
            metrics.put("status", "success");
            metricsLogger.info("Integração externa executada: {}", metrics);
            
            logger.info("Integração externa concluída - {}.{} - duração: {}ms", 
                    className, methodName, duration);
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            NewRelic.noticeError(e);
            NewRelic.incrementCounter("Custom/Integracao/" + className + "/Errors");
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("event", "integracao_externa_erro");
            metrics.put("integration", className + "." + methodName);
            metrics.put("duration_ms", duration);
            metrics.put("status", "error");
            metrics.put("error", e.getMessage());
            metricsLogger.error("Erro na integração externa: {}", metrics);
            
            logger.error("Erro na integração externa - {}.{}", className, methodName, e);
            throw e;
        }
    }
}
