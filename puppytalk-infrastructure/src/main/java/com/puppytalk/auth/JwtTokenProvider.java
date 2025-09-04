package com.puppytalk.auth;

import com.puppytalk.user.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증을 담당하는 구현체
 */
@Component
public class JwtTokenProvider implements TokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String USER_ID_CLAIM = "userId";
    private static final String USERNAME_CLAIM = "username";
    
    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    
    public JwtTokenProvider(@Value("${app.jwt.secret:puppytalk-super-secret-key-for-jwt-token-generation-minimum-256-bits}") String secret,
                           @Value("${app.jwt.access-token-validity:86400000}") long accessTokenValidityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
    }
    
    @Override
    public JwtToken generateToken(UserId userId, String username) {
        Date now = new Date();
        Date tokenExpiry = new Date(now.getTime() + accessTokenValidityInMilliseconds);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userId.value());
        claims.put(USERNAME_CLAIM, username);
        
        String accessToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(tokenExpiry)
            .signWith(secretKey)
            .compact();
        
        return new JwtToken(
            accessToken,
            convertToLocalDateTime(tokenExpiry)
        );
    }
    
    @Override
    public UserId getUserIdFromToken(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Long userId = claims.get(USER_ID_CLAIM, Long.class);
        
        if (userId == null) {
            throw InvalidTokenException.invalidToken();
        }
        
        return UserId.from(userId);
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.info("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.info("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.info("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.info("JWT token compact of handler are invalid: {}", e.getMessage());
        }
        return false;
    }
    
    
    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
}