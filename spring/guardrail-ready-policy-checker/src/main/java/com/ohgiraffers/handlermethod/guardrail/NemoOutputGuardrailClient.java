package com.ohgiraffers.handlermethod.guardrail;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class NemoOutputGuardrailClient implements OutputGuardrailClient {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public NemoOutputGuardrailClient(
            RestClient.Builder builder,
            @Value("${guardrails.base-url}") String baseUrl,
            MeterRegistry meterRegistry
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.meterRegistry = meterRegistry;
    }

    @Override
    public OutputGuardrailResult check(String response) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            NemoCheckResponse result = restClient.post()
                    .uri("/v1/output-rails/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NemoCheckRequest(List.of(new NemoMessage("assistant", response))))
                    .retrieve()
                    .body(NemoCheckResponse.class);

            if (result == null) {
                throw new IllegalStateException("NeMo Guardrails returned an empty output response");
            }

            Counter.builder("ai.output.rail.decisions")
                    .tag("status", result.status())
                    .tag("rail", result.rail() == null ? "none" : result.rail())
                    .register(meterRegistry)
                    .increment();
            return new OutputGuardrailResult(result.status(), result.content(), result.rail());
        } catch (RuntimeException exception) {
            Counter.builder("ai.output.rail.errors")
                    .tag("type", exception.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            throw exception;
        } finally {
            sample.stop(Timer.builder("ai.output.rail.duration")
                    .description("NeMo output rail request duration")
                    .register(meterRegistry));
        }
    }

    private record NemoCheckRequest(List<NemoMessage> messages) {
    }

    private record NemoMessage(String role, String content) {
    }

    private record NemoCheckResponse(String status, String content, String rail) {
    }
}
