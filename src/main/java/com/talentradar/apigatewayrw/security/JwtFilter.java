package com.talentradar.apigatewayrw.security;

import java.security.Key;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info("JwtFilter processing request for path: {}", path);

        HttpCookie tokenCookie = exchange.getRequest().getCookies().getFirst("token");

        if (tokenCookie != null) {
            log.info("Token cookie found: {}", tokenCookie.getValue().substring(0, 20) + "...");

            String jwt = tokenCookie.getValue();

            try {
                Claims claims = Jwts.parser()
                        .verifyWith((SecretKey) key())
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                String username = claims.get("username", String.class); // This might be null in your JWT
                String fullName = claims.get("fullName", String.class);
                String email = claims.get("email", String.class);

                log.info("JWT parsed successfully - UserId: {}, Role: {}, Email: {}, FullName: {}, Username: {}",
                        userId, role, email, fullName, username);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Role", role)
                        .header("X-User-Email", email)
                        .header("X-User-FullName", fullName)
                        .header("X-User-UserName", username != null ? username : "")  // Handle null username
                        .build();

                log.info("Added headers to request for path: {}", path);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException ex) {
                log.error("JWT validation failed: {}", ex.getMessage());
            }
        } else {
            log.warn("No token cookie found in request for path: {}", path);
        }

        return chain.filter(exchange);
    }

    // Key encryption
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}