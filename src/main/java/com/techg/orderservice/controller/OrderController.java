package com.techg.orderservice.controller;

import com.techg.orderservice.dto.OrderRequestDto;
import com.techg.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
//@RequiredArgsConstructor  this lombok annotation is a shorthand of manually creating a constructor
public class OrderController {

    //Inject your OrderService here
    private  final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")   //implementing circuit breaker logic
    @TimeLimiter(name = "inventory")
    @Retry(name="inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequestDto orderRequestDto){
       return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequestDto)) ;
    }

    public CompletableFuture<String>  fallbackMethod(OrderRequestDto orderRequestDto, RuntimeException runtimeException){
        return CompletableFuture.supplyAsync(()->"Oops! Something went wrong, please order after some time!");

    }
}
