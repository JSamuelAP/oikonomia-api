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

  private static final short MIN_SECRET_BYTES = 32;

  public JwtService(JwtProperties properties) {
    secretKey = buildSecretKey(properties.secret());
  }

  public String generate(String subject, Map<String, Object> claims, Duration ttl) {
    Instant now = Instant.now();
    return Jwts.builder().subject(subject).claims(claims).id(UUID.randomUUID().toString()).issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(ttl))).signWith(secretKey).compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  private static SecretKey buildSecretKey(String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET no está configurado");
    }

    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
        "JWT_SECRET es demasiado corto (%d bytes). Se requieren al menos %d bytes (256 bits) para HS256"
          .formatted(secretBytes.length, MIN_SECRET_BYTES));
    }

    return Keys.hmacShaKeyFor(secretBytes);
  }
}
