package com.poti.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:poti2024}")
    private String secret;

    @Value("${jwt.expire:86400000}")
    private Long expire;

    public String generateToken(Long userId, String openid) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("openid", openid)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getOpenidFromToken(String token) {
        return parseToken(token).get("openid", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            String openid = claims.get("openid", String.class);
            return generateToken(userId, openid);
        } catch (Exception e) {
            return null;
        }
    }
}