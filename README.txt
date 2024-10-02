WHY SPRING-CLOUD-SLEUTH?
========================
Assume we are having a distributed system of microservices that perform a particular business requirement. (see img-1.png)

Also please note that there can be many instance of each microservice.

If an issue occurred in a particular microservice we need to identify that microservice.
For that we can track the entire request chain. But it's not easy(Having many instances make this more complex).

To resolve this issue of finding the misbehaving microservice instance we can use "spring-cloud-sleuth" with "zipkin"



HOW SPRING-CLOUD-SLEUTH & ZIPKIN HELPS US TO TRACK THE REQUEST FLOW?
====================================================================

if we add the spring-cloud-sleuth and zipkin in our microservices, for each request, spring-cloud-sleuth generate one metadata. and that element consist 4 elements.

1. service name (micro-service instance name)
2. Trace Id (unique id that remains same throughout the )microservices for particular request)
3. Span Id (unique for a microservice)
4. Export Flag



IMPLEMENTATION STEPS
====================

1. start the zipkin server

docker run -d -p 9411:9411 openzipkin/zipkin

after that we can go to zipkin dashboard using this.
http://localhost:9411

2. Next we need to register our microservices(order-ms and payment-ms) in zipkin

add below two dependencies to each microservice.
spring-cloud-starter-sleuth
spring-cloud-starter-zipkin

3. Next in every microservice, we need to tell them where the zipkin server is up and running

for that add below property to application.properties file

spring.zipkin.base-url = http://localhost:9411

After this change restart the microservices
(note: until you make a request, we will not see microservices in zipkin dashboard)

4.Then make a request which makes a REST call from one service to another.

Then in the console logs of each microservice we can see that [service name|trace id|span id|export flag] has been added

Then If we check the trace id of both services, they will be same.


5. Now refresh the zipkin dashboard. Then we will see the microservies.

Using this dashboard we can understand 
1. the entire request flow
2. What are the endpoints called and HTTP methods used
3. Time taken to each REST call 



=======================================================================

In my local machine I get below Error

***************************
APPLICATION FAILED TO START
***************************

Description:

Your project setup is incompatible with our requirements due to following reasons:

- Spring Cloud Sleuth is not compatible with this Spring Cloud release train


EXPLANATION
===========

Starting with Spring Boot 3.x, Spring Cloud Sleuth has been deprecated and replaced by Micrometer Tracing. If you are using Spring Boot 3.x and Spring Cloud 2023.x, you should migrate away from Sleuth to Micrometer for distributed tracing.



---------------- Using Micrometer instead of Spring Cloud Sleuth------------------

Since Spring Cloud Sleuth is deprecated with Spring Boot 3+, This demo will show how to use micrometer for distributed log tracing


--------------- New Implementation Steps -----------------------------------------

1. implement 3 microservices as 
product-service - 8081
user-service - 8082
order-service - 8083


2. add below dependencies to every microservice

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
			<version>3.3.1</version>
		</dependency>

		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing</artifactId>
			<version>1.3.1</version>
		</dependency>

		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing-bridge-brave</artifactId> 
			<version>1.3.1</version>
		</dependency>


3. add below property to application.yml file

management:
  tracing:
    enabled: true


4. implement the HttpRequest headers as follows


package com.learn.order_service.controller;

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
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final Tracer tracer;  // Injected tracer to get trace information

    @GetMapping("/details")
    public ResponseEntity<String> getData() throws IOException, InterruptedException {
        log.info("request received to order service");

        // Get the current span and trace context
        var currentSpan = tracer.currentSpan();

        if (currentSpan == null) {
            throw new IllegalStateException("No active span found, tracing is not working");
        }

        String responseFromUserService = callUserService(currentSpan);
        return ResponseEntity.ok("Hello from order-service..." + "|" + responseFromUserService);
    }

    private String callUserService(Span currentSpan) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest userRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8082/user/details"))
                .header("X-B3-TraceId", currentSpan.context().traceId())
                .header("X-B3-SpanId", currentSpan.context().spanId())
                .header("X-B3-Sampled", currentSpan.context().sampled() ? "1" : "0")
                .GET()
                .build();

        HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());

        return userResponse.body();
    }
}



5. Then make an api call to order-service.

http://localhost:8083/order/details

Then api call chaining happens as follows

order-service -> user-service -> product-service


LOG OF PRODUCT SERVICE
======================

2024-10-02T14:30:46.647+05:30  INFO 32409 --- [product-service] [nio-8081-exec-1] [66fd0bbe9d64dd3cf6ecd43cd728c7ab-d1e1cfb576906791] c.l.p.controller.ProductController       : request received to product service


LOG OF USER SERVICE
======================

2024-10-02T14:30:46.396+05:30  INFO 32547 --- [user-service] [nio-8082-exec-1] [66fd0bbe9d64dd3cf6ecd43cd728c7ab-5621facd8e0c5866] c.l.u.controller.UserController          : request received to user service

LOG OF ORDER SERVICE
======================

2024-10-02T14:30:46.113+05:30  INFO 32702 --- [order-service] [nio-8083-exec-1] [66fd0bbe9d64dd3cf6ecd43cd728c7ab-f6ecd43cd728c7ab] c.l.o.controller.OrderController         : request received to order service


In the above log lines we can see "66fd0bbe9d64dd3cf6ecd43cd728c7ab" is common to every microservice. That is the trace id.
Also there is another Id which is unique to each microservice. That is spanId


Yeeee.... Thats All related to destributed tracing using micrometer

















