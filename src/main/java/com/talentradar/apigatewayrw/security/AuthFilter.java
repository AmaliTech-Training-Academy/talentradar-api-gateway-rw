package com.talentradar.apigatewayrw.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.apigatewayrw.dto.APIResponse;

import reactor.core.publisher.Mono;

public class AuthFilter implements GatewayFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String userId = request.getHeaders().getFirst("X-User-Id");
        ServerHttpResponse response = exchange.getResponse();

        if (userId == null || userId.isEmpty()) {

            try {

                APIResponse<Void> errorResponse = APIResponse.<Void>builder()
                        .status(false)
                        .message("Unauthorized")
                        .errors(List.of(Map.of("message", "Authentication token is missing or invalid")))
                        .build();
                String errorBody = objectMapper.writeValueAsString(errorResponse);
                DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                response.getHeaders().add("Content-Type", "application/json");

                return exchange.getResponse().writeWith(Mono.just(buffer));
            } catch (JsonProcessingException e) {
                e.printStackTrace();

                String fallbackJson = """
                        {
                          "status": false,
                          "message": "Server Error",
                          "errors": [
                            { "message": "Failed to process error response" }
                          ],
                          "data": null
                        }
                        """;

                DataBuffer buffer = response.bufferFactory().wrap(fallbackJson.getBytes(StandardCharsets.UTF_8));
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type", "application/json");

                return response.writeWith(Mono.just(buffer));
            }
        }

        return chain.filter(exchange);
    }

}
