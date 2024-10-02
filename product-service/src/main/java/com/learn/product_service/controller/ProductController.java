package com.learn.product_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @GetMapping("/details")
    public ResponseEntity<String> getData(){
        log.info("request received to product service");
        return ResponseEntity.ok("Hello from product-service...");
    }
}
