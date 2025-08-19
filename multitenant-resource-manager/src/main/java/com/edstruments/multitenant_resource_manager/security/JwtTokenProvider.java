package com.edstruments.multitenant_resource_manager.security;

import com.edstruments.multitenant_resource_manager.entity.User;
import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .claim("tenantId", userPrincipal.getTenantId())
                .claim("role", userPrincipal.getRole().name())
                .claim("authorities", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.parseLong(claims.getSubject());
        Long tenantId = claims.get("tenantId", Long.class);
        String role = claims.get("role", String.class);
        
        List<String> authorities = claims.get("authorities", List.class);
        List<GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserPrincipal principal = new UserPrincipal(
                userId,
                "", // username not needed after authentication
                "", // password not needed
                tenantId,
                User.Role.valueOf(role),
                grantedAuthorities);

        return new UsernamePasswordAuthenticationToken(principal, token, grantedAuthorities);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
//            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
//            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
//            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
           // logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
//            logger.error("JWT claims string is empty");
        }
        return false;
    }
}