package br.com.grupo99.oficinaservice.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Serviço para coletar e registrar métricas customizadas de negócio.
 * Expõe métricas para Prometheus e New Relic.
 */
@Component
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        // Contadores de Ordens de Serviço
        createCounter("ordem_servico.criadas.total", "Total de ordens de serviço criadas");
        createCounter("ordem_servico.atualizadas.total", "Total de ordens de serviço atualizadas");
        createCounter("ordem_servico.concluidas.total", "Total de ordens de serviço concluídas");
        createCounter("ordem_servico.canceladas.total", "Total de ordens de serviço canceladas");
        
        // Contadores por status
        createCounter("ordem_servico.status.diagnostico", "Ordens em Diagnóstico");
        createCounter("ordem_servico.status.execucao", "Ordens em Execução");
        createCounter("ordem_servico.status.finalizacao", "Ordens em Finalização");
        createCounter("ordem_servico.status.concluida", "Ordens Concluídas");
        
        // Contadores de erros
        createCounter("ordem_servico.erros.criacao", "Erros na criação");
        createCounter("ordem_servico.erros.atualizacao", "Erros na atualização");
        createCounter("ordem_servico.erros.integracao", "Erros de integração");
        
        // Timers para latência
        createTimer("ordem_servico.criacao.tempo", "Tempo de criação de OS");
        createTimer("ordem_servico.atualizacao.tempo", "Tempo de atualização de OS");
        createTimer("ordem_servico.consulta.tempo", "Tempo de consulta de OS");
        
        // Timers por status
        createTimer("ordem_servico.status.diagnostico.tempo", "Tempo no status Diagnóstico");
        createTimer("ordem_servico.status.execucao.tempo", "Tempo no status Execução");
        createTimer("ordem_servico.status.finalizacao.tempo", "Tempo no status Finalização");
    }

    private void createCounter(String name, String description) {
        Counter counter = Counter.builder("oficina." + name)
                .description(description)
                .tag("service", "oficina-service")
                .register(meterRegistry);
        counters.put(name, counter);
    }

    private void createTimer(String name, String description) {
        Timer timer = Timer.builder("oficina." + name)
                .description(description)
                .tag("service", "oficina-service")
                .register(meterRegistry);
        timers.put(name, timer);
    }

    // Métodos públicos para incrementar contadores
    
    public void incrementOrdemServicoCriada() {
        incrementCounter("ordem_servico.criadas.total");
    }

    public void incrementOrdemServicoAtualizada() {
        incrementCounter("ordem_servico.atualizadas.total");
    }

    public void incrementOrdemServicoConcluida() {
        incrementCounter("ordem_servico.concluidas.total");
    }

    public void incrementOrdemServicoCancelada() {
        incrementCounter("ordem_servico.canceladas.total");
    }

    public void incrementOrdemServicoStatus(String status) {
        incrementCounter("ordem_servico.status." + status.toLowerCase());
    }

    public void incrementErro(String tipoErro) {
        incrementCounter("ordem_servico.erros." + tipoErro);
    }

    private void incrementCounter(String name) {
        Counter counter = counters.get(name);
        if (counter != null) {
            counter.increment();
        }
    }

    // Métodos para registrar tempo de execução
    
    public void recordCriacaoTempo(long durationMs) {
        recordTiming("ordem_servico.criacao.tempo", durationMs);
    }

    public void recordAtualizacaoTempo(long durationMs) {
        recordTiming("ordem_servico.atualizacao.tempo", durationMs);
    }

    public void recordConsultaTempo(long durationMs) {
        recordTiming("ordem_servico.consulta.tempo", durationMs);
    }

    public void recordStatusTempo(String status, long durationMs) {
        recordTiming("ordem_servico.status." + status.toLowerCase() + ".tempo", durationMs);
    }

    private void recordTiming(String name, long durationMs) {
        Timer timer = timers.get(name);
        if (timer != null) {
            timer.record(Duration.ofMillis(durationMs));
        }
    }

    // Métricas de gauge (valores atuais)
    
    public void setOrdensServicoAtivas(int quantidade) {
        meterRegistry.gauge("oficina.ordem_servico.ativas.quantidade", quantidade);
    }

    public void setOrdensServicoPorStatus(String status, int quantidade) {
        meterRegistry.gauge("oficina.ordem_servico." + status.toLowerCase() + ".quantidade", quantidade);
    }
}
