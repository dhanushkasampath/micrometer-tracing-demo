package com.learn.order_service.controller;

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
@RequestMapping("/order")
public class OrderController {

    @GetMapping("/details")
    public ResponseEntity<String> getData() throws IOException, InterruptedException {
        log.info("request received to order service");

        String responseFromUserService = callUserService();
        return ResponseEntity.ok("Hello from order-service..." + "|" + responseFromUserService);
    }

    private String callUserService() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest userRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8082/user/details"))
//                .header("X-B3-TraceId", currentSpan.context().traceId())
//                .header("X-B3-SpanId", currentSpan.context().spanId())
//                .header("X-B3-Sampled", currentSpan.context().sampled() ? "1" : "0")
                .GET()
                .build();

        HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());

        return userResponse.body();
    }
}
