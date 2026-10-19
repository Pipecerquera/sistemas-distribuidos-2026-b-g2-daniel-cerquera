package barbersaas.appointment.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Reads or starts the W3C trace context of each request, logs it and returns it (log contract, CP2). */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceContextFilter extends OncePerRequestFilter {

    public static final String TRACEPARENT = "traceparent";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        TraceContext span = TraceContext.serverSpan(request.getHeader(TRACEPARENT));
        String correlationId = request.getHeader(CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("trace_id", span.traceId());
        MDC.put("span_id", span.spanId());
        MDC.put("correlation_id", correlationId);
        MDC.put("request_start_ms", Long.toString(start));
        response.setHeader(TRACEPARENT, span.traceparent());
        response.setHeader(CORRELATION_ID, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            log.atInfo()
                    .addKeyValue("event", "http.request.completed")
                    .addKeyValue("http_status", response.getStatus())
                    .addKeyValue("duration_ms", System.currentTimeMillis() - start)
                    .log("{} {} answered", request.getMethod(), request.getRequestURI());
            MDC.clear();
        }
    }
}
