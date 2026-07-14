package com.a0000.digicompass.common.security;

import com.a0000.digicompass.modules.auth.dto.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final String secret;
    private final long expirationMinutes;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String createToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds());
        return Jwts.builder()
                .subject(loginUser.username())
                .claim("uid", loginUser.id())
                .claim("nickname", loginUser.nickname())
                .claim("role", loginUser.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number userId = claims.get("uid", Number.class);
        return new LoginUser(
                userId.longValue(),
                claims.getSubject(),
                claims.get("nickname", String.class),
                claims.get("role", String.class)
        );
    }

    public long expirationSeconds() {
        return expirationMinutes * 60;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
