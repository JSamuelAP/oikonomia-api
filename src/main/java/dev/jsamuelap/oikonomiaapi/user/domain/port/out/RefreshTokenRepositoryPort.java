package dev.jsamuelap.oikonomiaapi.user.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.user.domain.model.RefreshToken;

public interface RefreshTokenRepositoryPort {
  RefreshToken save(RefreshToken refreshToken);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  void revokeAllByUserId(UUID userId);
}
