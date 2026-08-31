package dev.jsamuelap.oikonomiaapi.user.domain.port.out;

import java.time.Duration;
import java.util.UUID;

public interface TokenGeneratorPort {
  String generateAccessToken(UUID userId, String email);

  String generateRefreshToken(UUID userId);

  Duration accessTokenTtl();

  Duration refreshTokenTtl();
}
