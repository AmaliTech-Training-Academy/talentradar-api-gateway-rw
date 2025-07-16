package com.talentradar.apigatewayrw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import com.talentradar.apigatewayrw.security.AuthFilter;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayRwApplication {
    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("lb://user-service"))
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f.filter(new AuthFilter()))
                        .uri("lb://user-service"))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayRwApplication.class, args);
    }

}
