package lambdavalida.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class StructuredLogger {
    private final Logger logger;
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void info(String event, Map<String, Object> ctx) {
        log("INFO", event, ctx, null);
    }

    public void warn(String event, Map<String, Object> ctx) {
        log("WARN", event, ctx, null);
    }

    public void error(String event, Map<String, Object> ctx, Throwable t) {
        log("ERROR", event, ctx, t);
    }

    private void log(String level, String event, Map<String, Object> ctx, Throwable t) {
        try {
            Map<String, Object> log = new HashMap<>();
            log.put("timestamp", Instant.now().toString());
            log.put("level", level);
            log.put("event", event);
            log.put("service", "ValidaPessoa");
            if (ctx != null) log.putAll(ctx);
            if (t != null) {
                Map<String, Object> err = new HashMap<>();
                err.put("class", t.getClass().getName());
                err.put("message", t.getMessage());
                log.put("exception", err);
            }
            String json = mapper.writeValueAsString(log);
            if ("ERROR".equals(level)) {
                if (t != null) logger.error(json, t);
                else logger.error(json);
            } else if ("WARN".equals(level)) {
                logger.warn(json);
            } else {
                logger.info(json);
            }
        } catch (Exception e) {
            logger.error("Log failed", e);
        }
    }
}
