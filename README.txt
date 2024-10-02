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
product-service
user-service
order-service



















