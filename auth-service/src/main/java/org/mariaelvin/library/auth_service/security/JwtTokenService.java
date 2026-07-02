package org.mariaelvin.library.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.mariaelvin.library.auth_service.entity.AuthUser;
import org.mariaelvin.library.auth_service.entity.Permission;
import org.mariaelvin.library.auth_service.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for generating and validating JWT tokens.
 * 
 * Follows Spring Security 6.x recommendations: stateless sessions and token-based auth.
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // ✅ UPDATED: Generate token with ROLES + PERMISSIONS
    public String generateToken(AuthUser user) {

        Map<String, Object> claims = new HashMap<>();

        // ✅ Extract roles
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        // ✅ Extract permissions
        List<String> permissions = user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        claims.put("roles", roles);              // ✅ NEW
        claims.put("permissions", permissions);  // ✅ NEW
        claims.put("userId", user.getId());

        return buildToken(claims, user.getEmail());
    }

    public String buildToken(Map<String, Object> claims, String subject) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        if (log.isDebugEnabled()) {
            log.debug("Generated JWT for subject: {} exp: {}", subject, expiry);
        }

        return token;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> claims.get("permissions", List.class));
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        final String username = extractUsername(token);
        return (username != null && username.equals(expectedUsername) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException ex) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}