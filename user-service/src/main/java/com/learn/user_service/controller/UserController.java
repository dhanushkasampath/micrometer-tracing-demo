package com.learn.user_service.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final Tracer tracer;  // Injected tracer to get trace information

    @GetMapping("/details")
    public ResponseEntity<String> getData() throws IOException, InterruptedException {
        log.info("request received to user service");

        // Get the current span and trace context
        var currentSpan = tracer.currentSpan();

        if (currentSpan == null) {
            throw new IllegalStateException("No active span found, tracing is not working");
        }

        String responseFromProductService = callProductService(currentSpan);
        return ResponseEntity.ok("Hello from user-service..." + "|" + responseFromProductService);
    }

    private String callProductService(Span currentSpan) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest productRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8081/product/details"))
                .header("X-B3-TraceId", currentSpan.context().traceId())
                .header("X-B3-SpanId", currentSpan.context().spanId())
                .header("X-B3-Sampled", currentSpan.context().sampled() ? "1" : "0")
                .GET()
                .build();

        HttpResponse<String> productResponse = httpClient.send(productRequest, HttpResponse.BodyHandlers.ofString());

        return productResponse.body();
    }
}
