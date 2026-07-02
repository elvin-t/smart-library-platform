package org.mariaelvin.library.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/actuator/health",
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        /*
                         * Custom JwtAuthenticationFilter handles actual JWT validation
                         * and route-level permission checks.
                         */
                        .anyExchange().permitAll()
                )

                .build();
    }
}