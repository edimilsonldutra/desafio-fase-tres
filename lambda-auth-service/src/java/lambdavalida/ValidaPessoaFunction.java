package lambdavalida;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import lambdavalida.model.AuthRequest;
import lambdavalida.model.AuthResponse;
import lambdavalida.model.Customer;
import lambdavalida.monitoring.MetricsCollector;
import lambdavalida.monitoring.StructuredLogger;
import lambdavalida.service.CustomerService;
import lambdavalida.service.DocumentoValidator;
import lambdavalida.service.JWTService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ValidaPessoaFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final CustomerService customerService;
    private final JWTService jwtService;
    private final MetricsCollector metricsCollector;
    private final StructuredLogger logger;

    public ValidaPessoaFunction() {
        this.customerService = new CustomerService();
        this.jwtService = new JWTService();
        this.metricsCollector = new MetricsCollector();
        this.logger = new StructuredLogger(ValidaPessoaFunction.class);

        NewRelic.setTransactionName(null, "/auth");
    }

    @Override
    @Trace(dispatcher = true)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        long startTime = System.currentTimeMillis();
        String requestId = context != null ? context.getAwsRequestId() : "test-request-" + UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        NewRelic.addCustomParameter("correlationId", correlationId);
        NewRelic.addCustomParameter("requestId", requestId);
        if (context != null) {
            NewRelic.addCustomParameter("functionName", context.getFunctionName());
        }

        logger.info("auth_request_started", Map.of(
                "correlationId", correlationId,
                "requestId", requestId,
                "timestamp", Instant.now().toString()
        ));

        try {
            String body = input.getBody();

            // Validate request body
            if (body == null || body.trim().isEmpty()) {
                logger.warn("empty_request_body", Map.of("correlationId", correlationId));
                return createErrorResponse(400, "Request body cannot be empty", correlationId);
            }

            AuthRequest authRequest = objectMapper.readValue(body, AuthRequest.class);

            NewRelic.addCustomParameter("cpf", maskCPF(authRequest.getCpf()));

            // Validate CPF
            long validationStart = System.currentTimeMillis();
            if (!DocumentoValidator.isValidCPF(authRequest.getCpf())) {
                long validationDuration = System.currentTimeMillis() - validationStart;
                metricsCollector.recordValidationFailure(validationDuration);
                NewRelic.noticeError("Invalid CPF format");

                logger.warn("cpf_validation_failed", Map.of(
                        "correlationId", correlationId,
                        "cpf", maskCPF(authRequest.getCpf())
                ));

                return createErrorResponse(400, "CPF inválido", correlationId);
            }

            long validationDuration = System.currentTimeMillis() - validationStart;
            metricsCollector.recordValidationSuccess(validationDuration);

            // Query customer
            long dbQueryStart = System.currentTimeMillis();
            Customer customer = customerService.findByCPF(authRequest.getCpf());
            long dbQueryDuration = System.currentTimeMillis() - dbQueryStart;

            metricsCollector.recordDatabaseQuery(dbQueryDuration);

            if (customer == null) {
                metricsCollector.recordCustomerNotFound();
                logger.warn("customer_not_found", Map.of("correlationId", correlationId));
                return createErrorResponse(404, "Cliente não encontrado", correlationId);
            }

            if (!"ACTIVE".equals(customer.getStatus())) {
                metricsCollector.recordInactiveCustomer();
                logger.warn("customer_inactive", Map.of("correlationId", correlationId));
                return createErrorResponse(403, "Cliente inativo", correlationId);
            }

            // Generate JWT
            long jwtStart = System.currentTimeMillis();
            String token = jwtService.generateToken(customer);
            long jwtDuration = System.currentTimeMillis() - jwtStart;

            metricsCollector.recordJWTGeneration(jwtDuration);

            AuthResponse response = new AuthResponse(token, customer);

            long totalDuration = System.currentTimeMillis() - startTime;
            metricsCollector.recordSuccessfulAuth(totalDuration);

            NewRelic.addCustomParameter("total_duration_ms", totalDuration);

            logger.info("auth_successful", Map.of(
                    "correlationId", correlationId,
                    "total_duration_ms", totalDuration
            ));

            return createSuccessResponse(response, correlationId);

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;

            metricsCollector.recordError(e.getClass().getSimpleName());
            NewRelic.noticeError(e);

            logger.error("auth_request_failed", Map.of(
                    "correlationId", correlationId,
                    "error", e.getClass().getSimpleName()
            ), e);

            return createErrorResponse(500, "Erro interno do servidor", correlationId);
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(AuthResponse response, String correlationId) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("X-Correlation-ID", correlationId);
            headers.put("Access-Control-Allow-Origin", "*");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(headers)
                    .withBody(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            NewRelic.noticeError(e);
            return createErrorResponse(500, "Erro ao serializar resposta", correlationId);
        }
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message, String correlationId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Correlation-ID", correlationId);
        headers.put("Access-Control-Allow-Origin", "*");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", message);
        errorBody.put("correlationId", correlationId);
        errorBody.put("timestamp", Instant.now().toString());

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody(objectMapper.writeValueAsString(errorBody));
        } catch (Exception e) {
            NewRelic.noticeError(e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(headers)
                    .withBody("{\"error\":\"Internal server error\"}");
        }
    }

    private String maskCPF(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***";
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}

