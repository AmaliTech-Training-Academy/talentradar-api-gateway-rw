package com.talentradar.apigatewayrw.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class SessionValidationFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(SessionValidationFilter.class);

    private final ReactiveRedisOperations<String, Object> redisOperations;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/swagger",
            "/v3/api-docs",
            "/actuator"
    );

    public SessionValidationFilter(ReactiveRedisOperations<String, Object> redisOperations) {
        this.redisOperations = redisOperations;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getURI().getPath();

        // Skip validation for public paths
        if (isPublicPath(requestPath)) {
            return chain.filter(exchange);
        }

        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst("SESSION");

        if (sessionCookie == null) {
            return unauthorized(exchange, "Missing session cookie");
        }

        String sessionId = new String(Base64.getDecoder().decode(sessionCookie.getValue()));
        String redisKey = "spring:session:sessions:" + sessionId;
        System.out.println("ID "+sessionId);

        return redisOperations.hasKey(redisKey)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        logger.info("Session is revoked or expired: {}", sessionId);
                        return unauthorized(exchange, "Session is revoked or expired");
                    }

                    logger.debug("Session valid: {}", sessionId);
                    return chain.filter(exchange);
                });
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"error\": \"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -2; // Run before JwtFilter (-1)
    }
}
