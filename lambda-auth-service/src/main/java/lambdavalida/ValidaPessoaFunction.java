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
import lambdavalida.model.Pessoa;
import lambdavalida.monitoring.MetricsCollector;
import lambdavalida.monitoring.StructuredLogger;
import lambdavalida.service.PessoaService;
import lambdavalida.service.DocumentoValidator;
import lambdavalida.service.JWTService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ValidaPessoaFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final PessoaService pessoaService;
    private final JWTService jwtService;
    private final MetricsCollector metricsCollector;
    private final StructuredLogger logger;

    public ValidaPessoaFunction() {
        this.pessoaService = new PessoaService();
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

            NewRelic.addCustomParameter("documento", maskDocumento(authRequest.getCpf()));

            // Validate documento (CPF ou CNPJ)
            long validationStart = System.currentTimeMillis();
            String documento = authRequest.getCpf(); // Campo ainda se chama cpf no request
            
            if (!DocumentoValidator.isValidCPF(documento) && !DocumentoValidator.isValidCNPJ(documento)) {
                long validationDuration = System.currentTimeMillis() - validationStart;
                metricsCollector.recordValidationFailure(validationDuration);
                NewRelic.noticeError("Invalid documento format");

                logger.warn("documento_validation_failed", Map.of(
                        "correlationId", correlationId,
                        "documento", maskDocumento(documento)
                ));

                return createErrorResponse(400, "Documento inválido (CPF ou CNPJ)", correlationId);
            }

            long validationDuration = System.currentTimeMillis() - validationStart;
            metricsCollector.recordValidationSuccess(validationDuration);

            // Query pessoa
            long dbQueryStart = System.currentTimeMillis();
            Pessoa pessoa = pessoaService.findByDocumento(documento);
            long dbQueryDuration = System.currentTimeMillis() - dbQueryStart;

            metricsCollector.recordDatabaseQuery(dbQueryDuration);

            if (pessoa == null) {
                metricsCollector.recordCustomerNotFound();
                logger.warn("pessoa_not_found", Map.of("correlationId", correlationId));
                return createErrorResponse(404, "Pessoa não encontrada", correlationId);
            }

            if (!"ACTIVE".equals(pessoa.getStatus())) {
                metricsCollector.recordInactiveCustomer();
                logger.warn("pessoa_inactive", Map.of("correlationId", correlationId));
                return createErrorResponse(403, "Pessoa inativa", correlationId);
            }

            // Generate JWT
            long jwtStart = System.currentTimeMillis();
            String token = jwtService.generateToken(pessoa);
            long jwtDuration = System.currentTimeMillis() - jwtStart;

            metricsCollector.recordJWTGeneration(jwtDuration);

            AuthResponse response = new AuthResponse(token, pessoa);

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

    private String maskDocumento(String documento) {
        if (documento == null || documento.length() < 11) return "***";
        
        // CPF: 11 dígitos -> 123.***.***-99
        if (documento.length() == 11) {
            return documento.substring(0, 3) + ".***.***-" + documento.substring(9);
        }
        
        // CNPJ: 14 dígitos -> 12.***.***/**** -99
        if (documento.length() == 14) {
            return documento.substring(0, 2) + ".***.***/**** -" + documento.substring(12);
        }
        
        return "***";
    }
}

