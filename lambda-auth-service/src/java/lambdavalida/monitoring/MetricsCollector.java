package lambdavalida.monitoring;

import com.newrelic.api.agent.NewRelic;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.concurrent.TimeUnit;

public class MetricsCollector {
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;

    public MetricsCollector() {
        MeterRegistry reg = new SimpleMeterRegistry();
        this.authSuccessCounter = Counter.builder("auth.success").register(reg);
        this.authFailureCounter = Counter.builder("auth.failure").register(reg);
    }

    public void recordValidationSuccess(long ms) {
        NewRelic.recordMetric("Custom/CPF/Validation/Success", 1);
        NewRelic.recordResponseTimeMetric("Custom/CPF/Validation/Duration", ms);
    }

    public void recordValidationFailure(long ms) {
        NewRelic.recordMetric("Custom/CPF/Validation/Failure", 1);
    }

    public void recordDatabaseQuery(long ms) {
        NewRelic.recordMetric("Custom/Database/Query/Count", 1);
        NewRelic.recordResponseTimeMetric("Custom/Database/Query/Duration", ms);
    }

    public void recordJWTGeneration(long ms) {
        NewRelic.recordMetric("Custom/JWT/Generation/Count", 1);
        NewRelic.recordResponseTimeMetric("Custom/JWT/Generation/Duration", ms);
    }

    public void recordSuccessfulAuth(long ms) {
        authSuccessCounter.increment();
        NewRelic.recordMetric("Custom/Auth/Success", 1);
        NewRelic.recordResponseTimeMetric("Custom/Auth/TotalDuration", ms);
    }

    public void recordCustomerNotFound() {
        authFailureCounter.increment();
        NewRelic.recordMetric("Custom/Customer/NotFound", 1);
    }

    public void recordInactiveCustomer() {
        authFailureCounter.increment();
        NewRelic.recordMetric("Custom/Customer/Inactive", 1);
    }

    public void recordError(String errorType) {
        NewRelic.recordMetric("Custom/Errors/Total", 1);
        NewRelic.recordMetric("Custom/Errors/" + errorType, 1);
    }
}
