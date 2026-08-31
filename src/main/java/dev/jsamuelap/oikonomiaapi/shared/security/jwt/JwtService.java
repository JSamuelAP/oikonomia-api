package dev.jsamuelap.oikonomiaapi.shared.security.jwt;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {
  private final SecretKey secretKey;

  public JwtService(JwtProperties properties) {
    secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generate(String subject, Map<String, Object> claims, Duration ttl) {
    Instant now = Instant.now();
    return Jwts.builder().subject(subject).claims(claims).id(UUID.randomUUID().toString()).issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(ttl))).signWith(secretKey).compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
