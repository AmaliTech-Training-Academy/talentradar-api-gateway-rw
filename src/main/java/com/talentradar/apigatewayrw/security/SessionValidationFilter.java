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
            "/api/v1/auth/complete-registration",
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

        if (isPublicPath(requestPath)) {
            return chain.filter(exchange);
        }

        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst("SESSION");

        if (sessionCookie == null) {
            return unauthorized(exchange, "Missing session cookie");
        }

        String sessionId;
        try {
            sessionId = new String(Base64.getDecoder().decode(sessionCookie.getValue()));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid session ID encoding", e);
            return unauthorized(exchange, "Invalid session ID");
        }

        String redisKey = "spring:session:sessions:" + sessionId;
        logger.info("Checking session ID: {}", sessionId);

        return redisOperations.hasKey(redisKey)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        logger.info("Session is revoked or expired: {}", sessionId);
                        return unauthorized(exchange, "Session is revoked or expired");
                    }
                    logger.debug("Session valid: {}", sessionId);
                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> {
                    logger.error("Redis unreachable, skipping session validation for path {}", requestPath, ex);
                    return chain.filter(exchange); // <--- allow the request through
                });
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = """
            {
              "status": false,
              "message": "Unauthorized",
              "data": null,
              "errors": [
                {
                  "message": "%s"
                }
              ]
            }
            """.formatted(message);

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
