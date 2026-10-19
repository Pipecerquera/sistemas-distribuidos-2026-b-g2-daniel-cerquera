package barbersaas.appointment.booking;

import barbersaas.appointment.trace.TraceContext;
import barbersaas.appointment.trace.TraceContextFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Outgoing call of the journey (CP1 step 6): schedule-api's free slots, in a child span of the request. */
@Component
public class ScheduleApiClient {

    private static final Logger log = LoggerFactory.getLogger(ScheduleApiClient.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final String baseUrl;

    public ScheduleApiClient(@Value("${schedule-api.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> freeStarts(UUID barberId, UUID serviceId, LocalDate date) {
        TraceContext call = TraceContext.current().child();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/availability?barberId=" + barberId
                        + "&serviceId=" + serviceId + "&date=" + date))
                .timeout(Duration.ofSeconds(3))
                .header(TraceContextFilter.TRACEPARENT, call.traceparent())
                .header(TraceContextFilter.CORRELATION_ID, MDC.get("correlation_id"))
                .GET()
                .build();
        long start = System.currentTimeMillis();
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            String event = e instanceof HttpTimeoutException ? "downstream.call.timeout" : "downstream.call.failed";
            log.atWarn().addKeyValue("event", event).addKeyValue("span_id", call.spanId())
                    .addKeyValue("downstream", "schedule-api")
                    .addKeyValue("duration_ms", System.currentTimeMillis() - start)
                    .log("Availability request failed: {}", e.getClass().getSimpleName());
            throw new Unavailable();
        }
        log.atInfo().addKeyValue("event", "downstream.call.succeeded").addKeyValue("span_id", call.spanId())
                .addKeyValue("downstream", "schedule-api").addKeyValue("http_status", response.statusCode())
                .addKeyValue("duration_ms", System.currentTimeMillis() - start)
                .log("Availability answered");
        if (response.statusCode() != 200) {
            throw new Unavailable();
        }
        try {
            List<String> starts = new ArrayList<>();
            for (JsonNode slot : JSON.readTree(response.body()).path("slots")) {
                starts.add(slot.path("startTime").asText());
            }
            return starts;
        } catch (IOException e) {
            throw new Unavailable();
        }
    }

    /** schedule-api did not answer usable free slots: the booking answers 503, never guesses. */
    public static class Unavailable extends RuntimeException {
    }
}
