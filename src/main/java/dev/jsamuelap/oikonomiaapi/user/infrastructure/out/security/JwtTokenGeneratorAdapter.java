package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.security;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtProperties;
import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtService;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenGeneratorPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {
  private final JwtService jwtService;
  private final JwtProperties properties;

  @Override
  public String generateAccessToken(UUID userId, String email) {
    return jwtService.generate(userId.toString(), Map.of("email", email, "type", "access"),
      properties.accessTokenTtl());
  }

  @Override
  public String generateRefreshToken(UUID userId) {
    return jwtService.generate(userId.toString(), Map.of("type", "refresh"), properties.refreshTokenTtl());
  }

  @Override
  public Duration accessTokenTtl() {
    return properties.accessTokenTtl();
  }

  @Override
  public Duration refreshTokenTtl() {
    return properties.refreshTokenTtl();
  }
}
