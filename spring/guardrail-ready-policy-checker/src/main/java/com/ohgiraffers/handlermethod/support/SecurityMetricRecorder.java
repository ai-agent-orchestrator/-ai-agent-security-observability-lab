package com.ohgiraffers.handlermethod.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SecurityMetricRecorder {

    private final MeterRegistry meterRegistry;

    public SecurityMetricRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordAuthFailure(HttpServletRequest request) {
        Counter.builder("security.auth.failures")
                .description("Total number of unauthenticated requests rejected by security")
                .tag("method", request.getMethod())
                .tag("uri", request.getRequestURI())
                .tag("status", "401")
                .register(meterRegistry)
                .increment();
    }

    public void recordAccessDenied(HttpServletRequest request) {
        Counter.builder("security.access.denied")
                .description("Total number of authenticated requests denied by security")
                .tag("method", request.getMethod())
                .tag("uri", request.getRequestURI())
                .tag("status", "403")
                .register(meterRegistry)
                .increment();
    }
}
