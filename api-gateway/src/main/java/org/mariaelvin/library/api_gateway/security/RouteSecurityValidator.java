package org.mariaelvin.library.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;

@Component
public class RouteSecurityValidator {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    );

    /*
     * Ordered rules are important.
     * More specific routes must come before generic routes.
     */
    private static final List<RoutePermissionRule> ROUTE_PERMISSION_RULES = List.of(

            // Auth Admin / Role Management APIs
            new RoutePermissionRule(HttpMethod.GET, "/api/auth/admin/**", "USER_READ"),
            new RoutePermissionRule(HttpMethod.POST, "/api/auth/admin/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.PUT, "/api/auth/admin/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/auth/admin/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.DELETE, "/api/auth/admin/**", "USER_WRITE"),

            new RoutePermissionRule(HttpMethod.GET, "/api/auth/roles/**", "USER_READ"),
            new RoutePermissionRule(HttpMethod.POST, "/api/auth/roles/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.PUT, "/api/auth/roles/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/auth/roles/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.DELETE, "/api/auth/roles/**", "USER_WRITE"),

            // User Service
            new RoutePermissionRule(HttpMethod.GET, "/api/users/**", "USER_READ"),
            new RoutePermissionRule(HttpMethod.POST, "/api/users/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.PUT, "/api/users/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/users/**", "USER_WRITE"),
            new RoutePermissionRule(HttpMethod.DELETE, "/api/users/**", "USER_WRITE"),

            // Inventory APIs - must be before generic book rules
            new RoutePermissionRule(HttpMethod.GET, "/api/books/inventory/**", "INVENTORY_READ"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/books/inventory/**", "INVENTORY_WRITE"),
            new RoutePermissionRule(HttpMethod.POST, "/api/books/inventory/**", "INVENTORY_WRITE"),
            new RoutePermissionRule(HttpMethod.PUT, "/api/books/inventory/**", "INVENTORY_WRITE"),
            new RoutePermissionRule(HttpMethod.DELETE, "/api/books/inventory/**", "INVENTORY_WRITE"),

            // Book Service - specific borrow/return routes first
            new RoutePermissionRule(HttpMethod.PATCH, "/api/books/*/borrow", "BORROW_WRITE"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/books/*/return", "RETURN_WRITE"),

            // Book Service - generic book routes
            new RoutePermissionRule(HttpMethod.GET, "/api/books/**", "BOOK_READ"),
            new RoutePermissionRule(HttpMethod.POST, "/api/books/**", "BOOK_WRITE"),
            new RoutePermissionRule(HttpMethod.PUT, "/api/books/**", "BOOK_WRITE"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/books/**", "BOOK_WRITE"),
            new RoutePermissionRule(HttpMethod.DELETE, "/api/books/**", "BOOK_WRITE"),

            // Borrow Service
            new RoutePermissionRule(HttpMethod.GET, "/api/borrow-records/*/fine", "BORROW_READ"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/borrow-records/*/fine/pay", "RETURN_WRITE"),

            new RoutePermissionRule(HttpMethod.GET, "/api/borrow-records/**", "BORROW_READ"),
            new RoutePermissionRule(HttpMethod.POST, "/api/borrow-records/**", "BORROW_WRITE"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/borrow-records/*/return", "RETURN_WRITE"),

            new RoutePermissionRule(HttpMethod.GET, "/api/notifications/**", "BORROW_READ"),
            new RoutePermissionRule(HttpMethod.PATCH, "/api/notifications/**", "BORROW_READ"),

            // Dashboard / Analytics
            new RoutePermissionRule(HttpMethod.GET, "/api/dashboard/**", "BOOK_READ")

            );

    public boolean isPublic(ServerWebExchange exchange) {

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return true;
        }

        String path = exchange.getRequest().getURI().getPath();

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    public Optional<String> requiredPermission(ServerWebExchange exchange) {

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        return ROUTE_PERMISSION_RULES.stream()
                .filter(rule ->
                        rule.method().equals(method)
                                && pathMatcher.match(rule.pathPattern(), path)
                )
                .map(RoutePermissionRule::permission)
                .findFirst();
    }

    private record RoutePermissionRule(
            HttpMethod method,
            String pathPattern,
            String permission
    ) {
    }
}