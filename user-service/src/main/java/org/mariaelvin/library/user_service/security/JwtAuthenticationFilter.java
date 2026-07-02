package org.mariaelvin.library.user_service.security;

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
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String bearerPrefix = "Bearer ";

        if (authHeader == null || !authHeader.startsWith(bearerPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(bearerPrefix.length());

        try {
            String email = jwtTokenService.extractUsername(jwt);
            Long userId = jwtTokenService.extractUserId(jwt);
            List<String> permissions = jwtTokenService.extractPermissions(jwt);

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

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Optional: store userId in request for controller/service use
                request.setAttribute("userId", userId);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}