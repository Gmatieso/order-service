package com.techg.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //create a bean of type web client  and identify it with name webClient
    @Bean
    public WebClient webClient(){
        return WebClient.builder().build();
    }
}
