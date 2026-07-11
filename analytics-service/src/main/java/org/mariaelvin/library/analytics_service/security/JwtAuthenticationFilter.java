package org.mariaelvin.library.analytics_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            log.debug("Authorization header is missing for URI: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.debug("Authorization header does not start with Bearer for URI: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String email = jwtTokenService.extractUsername(jwt);
            Long userId = jwtTokenService.extractUserId(jwt);
            List<String> roles = jwtTokenService.extractRoles(jwt);
            List<String> permissions = jwtTokenService.extractPermissions(jwt);

            if (roles == null) {
                roles = Collections.emptyList();
            }

            if (permissions == null) {
                permissions = Collections.emptyList();
            }

            if (email != null
                    && jwtTokenService.isTokenValid(jwt)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                List<GrantedAuthority> authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                /*
                 * These attributes are used by AnalyticsController and FeignClientConfig.
                 */
                request.setAttribute("userId", userId);
                request.setAttribute("email", email);
                request.setAttribute("roles", roles);
                request.setAttribute("permissions", permissions);

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug(
                        "Analytics authentication set for email={}, userId={}, roles={}, permissions={}",
                        email,
                        userId,
                        roles,
                        permissions
                );
            }

        } catch (Exception ex) {
            log.debug(
                    "JWT validation failed for URI: {}. Reason: {}",
                    request.getRequestURI(),
                    ex.getMessage()
            );
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html");
    }
}