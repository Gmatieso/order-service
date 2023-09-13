package com.techg.orderservice.service;

import com.techg.orderservice.dto.InventoryResponse;
import com.techg.orderservice.dto.OrderLineItemsDto;
import com.techg.orderservice.dto.OrderRequestDto;
import com.techg.orderservice.model.Order;
import com.techg.orderservice.model.OrderLineItems;
import com.techg.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
//@RequiredArgsConstructor  This lombok annotation is an alternative of manually creating a constructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public OrderService(OrderRepository orderRepository, WebClient webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = (WebClient.Builder) webClientBuilder;
    }

    public void placeOrder(OrderRequestDto orderRequestDto){
        //creates an instance of new Order
        Order order = new Order();
        //set random order number
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequestDto.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        //collect all skuCodes from order object
       List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        //call Inventory Service, and place order if product is in stock
        InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                                .block();   //adding synchronous communication

boolean allProductsInStock =  Arrays.stream(inventoryResponsesArray)
        .allMatch(InventoryResponse::isInStock);

        //if allProductsInStock is true  i.e product is in stock place order or else throw exception
        if(allProductsInStock){
            //save to the repository
            orderRepository.save(order);
        } else {
            throw  new IllegalArgumentException("Product is not in stock, please try again later");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
