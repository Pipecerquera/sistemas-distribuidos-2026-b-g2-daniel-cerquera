package barbersaas.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TracePropagationTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String CALLER_SPAN = "00f067aa0ba902b7";
    private static final AtomicReference<String> sentToSchedule = new AtomicReference<>();
    private static HttpServer scheduleApi;

    @Autowired
    private TestRestTemplate http;

    @DynamicPropertySource
    static void scheduleApi(DynamicPropertyRegistry registry) throws IOException {
        scheduleApi = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        scheduleApi.createContext("/api/v1/availability", exchange -> {
            sentToSchedule.set(exchange.getRequestHeaders().getFirst("traceparent"));
            byte[] body = "{\"slots\":[{\"startTime\":\"10:00\"}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        scheduleApi.start();
        registry.add("schedule-api.base-url", () -> "http://localhost:" + scheduleApi.getAddress().getPort());
    }

    @AfterAll
    static void stopScheduleApi() {
        scheduleApi.stop(0);
    }

    @Test
    void traceIdReceivedIsTheTraceIdSentToScheduleApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("traceparent", "00-" + TRACE_ID + "-" + CALLER_SPAN + "-01");
        Map<String, String> booking = Map.of("barberId", "a71c2f0e-6c1e-4d8a-9a56-0f3b8c2d9e11",
                "serviceId", "e2f4b6a8-1c3d-4e5f-8a9b-0c1d2e3f4a5b", "date", "2026-10-20", "startTime", "10:00");

        ResponseEntity<String> response =
                http.postForEntity("/api/v1/appointments", new HttpEntity<>(booking, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(sentToSchedule.get()).matches("00-" + TRACE_ID + "-[0-9a-f]{16}-01")
                .doesNotContain(CALLER_SPAN);
        assertThat(response.getHeaders().getFirst("traceparent")).startsWith("00-" + TRACE_ID + "-");
        assertThat(http.getForObject("/metrics", String.class))
                .contains("appointment_book_requests_total{outcome=\"created\"} 1.0")
                .contains("appointment_book_duration_seconds_bucket{le=\"0.8\"} 1");
    }
}
