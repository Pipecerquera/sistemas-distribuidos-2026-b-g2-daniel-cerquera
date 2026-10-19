package barbersaas.appointment.booking;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Instrumented slice of POST /api/v1/appointments: checks the slot with schedule-api, persistence is out of scope. */
@RestController
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final ScheduleApiClient schedule;
    private final BookingMetrics metrics;

    public BookingController(ScheduleApiClient schedule, BookingMetrics metrics) {
        this.schedule = schedule;
        this.metrics = metrics;
    }

    public record BookRequest(UUID barberId, UUID serviceId, LocalDate date, String startTime) {
    }

    @PostMapping("/api/v1/appointments")
    public ResponseEntity<Map<String, Object>> book(@RequestBody BookRequest req) {
        long start = System.nanoTime();
        String outcome = BookingMetrics.FAILED;
        try {
            log.atInfo().addKeyValue("event", "appointment.book.received").addKeyValue("barber_id", req.barberId())
                    .addKeyValue("service_id", req.serviceId()).log("Booking request received");
            if (!schedule.freeStarts(req.barberId(), req.serviceId(), req.date()).contains(req.startTime())) {
                outcome = BookingMetrics.REJECTED;
                log.atWarn().addKeyValue("event", "appointment.book.rejected").addKeyValue("outcome", outcome)
                        .addKeyValue("error_code", "BUSINESS_RULE_VIOLATION").log("Slot not offered for the barber");
                return error(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION",
                        "The barber does not offer that time on " + req.date());
            }
            outcome = BookingMetrics.CREATED;
            UUID id = UUID.randomUUID();
            log.atInfo().addKeyValue("event", "appointment.book.created").addKeyValue("outcome", outcome)
                    .addKeyValue("appointment_id", id).log("Appointment booked");
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "PENDING"));
        } catch (ScheduleApiClient.Unavailable e) {
            log.atError().addKeyValue("event", "appointment.book.failed").addKeyValue("outcome", outcome)
                    .addKeyValue("error_code", "SERVICE_UNAVAILABLE").log("schedule-api unavailable, nothing booked");
            return error(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Availability could not be checked");
        } finally {
            metrics.record(outcome, System.nanoTime() - start);
        }
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("error", code, "message", message, "traceId", MDC.get("correlation_id")));
    }
}
