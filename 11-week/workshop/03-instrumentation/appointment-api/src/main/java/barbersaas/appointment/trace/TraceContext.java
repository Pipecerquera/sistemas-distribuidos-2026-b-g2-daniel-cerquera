package barbersaas.appointment.trace;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;
import org.slf4j.MDC;

/** W3C trace context: one span of a trace, written as a {@code traceparent} header. */
public record TraceContext(String traceId, String spanId) {

    private static final Pattern TRACEPARENT = Pattern.compile("00-([0-9a-f]{32})-([0-9a-f]{16})-[0-9a-f]{2}");
    private static final String INVALID_TRACE = "0".repeat(32);
    private static final SecureRandom RANDOM = new SecureRandom();

    /** The server span of an incoming request: child of a valid {@code traceparent}, else a new trace. */
    public static TraceContext serverSpan(String traceparent) {
        var m = traceparent == null ? null : TRACEPARENT.matcher(traceparent.trim());
        if (m != null && m.matches() && !m.group(1).equals(INVALID_TRACE)) {
            return new TraceContext(m.group(1), randomHex(8));
        }
        return new TraceContext(randomHex(16), randomHex(8));
    }

    /** The server span of the request being served, as {@link TraceContextFilter} put it in the MDC. */
    public static TraceContext current() {
        return new TraceContext(MDC.get("trace_id"), MDC.get("span_id"));
    }

    /** A child span of this one, for an outgoing call. */
    public TraceContext child() {
        return new TraceContext(traceId, randomHex(8));
    }

    public String traceparent() {
        return "00-" + traceId + "-" + spanId + "-01";
    }

    private static String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }
}
