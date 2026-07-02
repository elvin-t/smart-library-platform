package org.mariaelvin.library.api_gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenService jwtTokenService;
    private final RouteSecurityValidator routeSecurityValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (routeSecurityValidator.isPublic(exchange)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtTokenService.isTokenValid(token)) {
                return unauthorized(exchange, "Invalid or expired JWT token");
            }

            String email = jwtTokenService.extractUsername(token);
            Long userId = jwtTokenService.extractUserId(token);
            List<String> roles = jwtTokenService.extractRoles(token);
            List<String> permissions = jwtTokenService.extractPermissions(token);

            String requiredPermission = routeSecurityValidator
                    .requiredPermission(exchange)
                    .orElse(null);

            if (requiredPermission != null &&
                    (permissions == null || !permissions.contains(requiredPermission))) {

                log.warn(
                        "Gateway access denied. path={}, requiredPermission={}, user={}",
                        exchange.getRequest().getURI().getPath(),
                        requiredPermission,
                        email
                );

                return forbidden(exchange, "You do not have permission to access this resource");
            }

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> builder
                            /*
                             * Forward original token.
                             * Downstream services can still validate JWT independently.
                             */
                            .header(HttpHeaders.AUTHORIZATION, authHeader)

                            /*
                             * Optional propagated headers.
                             * Useful for logging/audit.
                             */
                            .header("X-User-Id", userId == null ? "" : String.valueOf(userId))
                            .header("X-User-Email", email == null ? "" : email)
                            .header("X-Roles", roles == null ? "" : String.join(",", roles))
                            .header("X-Permissions", permissions == null ? "" : String.join(",", permissions))
                    )
                    .build();

            log.debug(
                    "Gateway JWT validated. userId={}, email={}, path={}",
                    userId,
                    email,
                    exchange.getRequest().getURI().getPath()
            );

            return chain.filter(mutatedExchange);

        } catch (Exception ex) {
            log.warn("Gateway JWT validation failed: {}", ex.getMessage());
            return unauthorized(exchange, "Invalid JWT token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "code": "SEC_001",
                  "message": "%s"
                }
                """.formatted(message);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "code": "SEC_002",
                  "message": "%s"
                }
                """.formatted(message);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}