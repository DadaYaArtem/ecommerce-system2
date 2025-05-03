package org.example.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.springframework.stereotype.Service;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Map;
import java.util.function.Supplier;

@Service
public class TraceService {

    private final Tracer tracer;

    public TraceService(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("org.example.order-service");
    }

    public <T> T traceOperation(String spanName, Supplier<T> operation) {
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (var scope = span.makeCurrent()) {
            T result = operation.get();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    public void traceOperation(String spanName, Runnable operation) {
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (var scope = span.makeCurrent()) {
            operation.run();
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    public void addSpanAttributes(Map<String, String> attributes) {
        Span span = Span.current();
        if (span.isRecording()) {
            attributes.forEach((key, value) -> 
                span.setAttribute(AttributeKey.stringKey(key), value));
        }
    }

    public void addSpanEvent(String eventName, Map<String, String> attributes) {
        Span span = Span.current();
        if (span.isRecording()) {
            Attributes eventAttributes = Attributes.empty();
            if (attributes != null && !attributes.isEmpty()) {
                AttributesBuilder builder = Attributes.builder();
                attributes.forEach((key, value) ->
                        builder.put(AttributeKey.stringKey(key), value));
                eventAttributes = builder.build();
            }
            span.addEvent(eventName, eventAttributes);
        }
    }
}