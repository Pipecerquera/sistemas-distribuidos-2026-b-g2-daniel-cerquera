package barbersaas.appointment.trace;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.event.KeyValuePair;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.core.env.Environment;

/** One JSON line per log event with the fields of the log contract (CP2, section 1). */
public class JsonLogFormatter implements StructuredLogFormatter<ILoggingEvent> {

    private static final DateTimeFormatter UTC_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String service;
    private final String version;
    private final String environment;

    public JsonLogFormatter(Environment env) {
        this.service = env.getProperty("spring.application.name");
        this.version = env.getProperty("app.version");
        this.environment = env.getProperty("app.environment");
    }

    @Override
    public String format(ILoggingEvent e) {
        Map<String, String> mdc = e.getMDCPropertyMap();
        Map<String, Object> fields = new LinkedHashMap<>();
        if (e.getKeyValuePairs() != null) {
            for (KeyValuePair pair : e.getKeyValuePairs()) {
                fields.put(pair.key, pair.value);
            }
        }
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("timestamp", UTC_MILLIS.format(e.getInstant()));
        line.put("level", e.getLevel().toString());
        line.put("service", service);
        line.put("version", version);
        line.put("environment", environment);
        line.put("trace_id", mdc.get("trace_id"));
        line.put("span_id", fields.getOrDefault("span_id", mdc.get("span_id")));
        line.put("correlation_id", mdc.get("correlation_id"));
        line.put("event", fields.getOrDefault("event", "log"));
        line.put("message", e.getFormattedMessage());
        line.put("duration_ms", fields.getOrDefault("duration_ms", elapsed(mdc, e.getTimeStamp())));
        fields.forEach(line::putIfAbsent);
        try {
            return JSON.writeValueAsString(line) + "\n";
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Outside a closing line, duration_ms is the time elapsed since the request started (0 outside a request). */
    private static long elapsed(Map<String, String> mdc, long timestamp) {
        String start = mdc.get("request_start_ms");
        return start == null ? 0 : timestamp - Long.parseLong(start);
    }
}
