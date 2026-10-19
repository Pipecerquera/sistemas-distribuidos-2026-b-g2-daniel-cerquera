package barbersaas.appointment.booking;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Booking metrics of CP4 and the GET /metrics endpoint that Prometheus scrapes. */
@RestController
public class BookingMetrics {

    public static final String CREATED = "created";
    public static final String REJECTED = "rejected";
    public static final String FAILED = "failed";

    private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private final Timer duration = Timer.builder("appointment_book_duration")
            .description("Time to answer POST /api/v1/appointments")
            .serviceLevelObjectives(seconds(0.05), seconds(0.1), seconds(0.2), seconds(0.3), seconds(0.5),
                    seconds(0.8), seconds(1), seconds(2), seconds(3), seconds(6))
            .register(registry);

    public BookingMetrics() {
        for (String outcome : new String[] {CREATED, REJECTED, FAILED}) {
            counter(outcome);
        }
    }

    public void record(String outcome, long nanos) {
        counter(outcome).increment();
        duration.record(nanos, TimeUnit.NANOSECONDS);
    }

    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    public String scrape() {
        return registry.scrape();
    }

    private Counter counter(String outcome) {
        return Counter.builder("appointment_book_requests")
                .description("Booking requests by outcome")
                .tag("outcome", outcome)
                .register(registry);
    }

    private static Duration seconds(double s) {
        return Duration.ofMillis((long) (s * 1000));
    }
}
