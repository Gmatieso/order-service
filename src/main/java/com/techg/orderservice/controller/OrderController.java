package com.techg.orderservice.controller;

import com.techg.orderservice.dto.OrderRequestDto;
import com.techg.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public String placeOrder(@RequestBody OrderRequestDto orderRequestDto){
        orderService.placeOrder(orderRequestDto);
        return "Order Placed Successfully";
    }

    public String fallbackMethod(OrderRequestDto orderRequestDto, RuntimeException runtimeException){
        return "Oops! Something went wrong, please order after some time!";

    }
}
