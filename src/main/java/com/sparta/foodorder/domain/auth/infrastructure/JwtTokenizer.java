package com.sparta.foodorder.domain.auth.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import com.sparta.foodorder.domain.user.domain.UserStatus;
import com.sparta.foodorder.domain.user.domain.UserRole;

@Component
@Slf4j
public class JwtTokenizer {
    private final byte[] accessSecret;
    private final byte[] refreshSecret;

    public static Long ACCESS_TOKEN_EXPIRE_COUNT= 1000 * 60 * 300L;
    public static Long REFRESH_TOKEN_EXPIRE_COUNT=7*24*60*60*1000L;

    public JwtTokenizer(@Value("${jwt.secretKey}") String accessSecret, @Value("${jwt.refreshKey}") String refreshSecret) {
        this.accessSecret = accessSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshSecret = refreshSecret.getBytes(StandardCharsets.UTF_8);
    }

    private String createToken(Long id, String email, UserStatus status, Long expire, byte[] secretKey, Set<UserRole> roles) {

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("status", status);
        claims.put("userEmail", email);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()+expire))
                .signWith(getSignKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAccessToken(Long id, String email, UserStatus status, Set<UserRole> roles) {
        return createToken(id, email, status, ACCESS_TOKEN_EXPIRE_COUNT, accessSecret, roles);
    }

    public String createRefreshToken(Long id, String email, UserStatus status, Set<UserRole> roles) {
        return createToken(id, email, status, REFRESH_TOKEN_EXPIRE_COUNT, refreshSecret, roles);
    }

    private static SecretKey getSignKey(byte[] secretKey) {
        return Keys.hmacShaKeyFor(secretKey);
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey(accessSecret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey(refreshSecret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getRemainingTime(String token) {
        Claims claims = parseAccessToken(token);
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

}
