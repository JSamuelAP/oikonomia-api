package dev.jsamuelap.oikonomiaapi.user.domain.model;

import java.time.Instant;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

import lombok.Getter;

@Getter
public final class RefreshToken {
  private final UUID id;
  private final UUID userId;
  private final String tokenHash;
  private final Instant expiresAt;
  private boolean revoked;
  private final Instant createdAt;
  private UUID replacedBy;

  private RefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked, Instant createdAt,
    UUID replacedBy) {
    this.id = id;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.revoked = revoked;
    this.createdAt = createdAt;
    this.replacedBy = replacedBy;
  }

  public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
    validateUserId(userId);
    validateTokenHash(tokenHash);
    validateExpiresAt(expiresAt);

    return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, false, Instant.now(), null);
  }

  public static RefreshToken reconstitute(UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked,
    Instant createdAt, UUID replacedBy) {
    return new RefreshToken(id, userId, tokenHash, expiresAt, revoked, createdAt, replacedBy);
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !revoked && !isExpired();
  }

  public void revoke() {
    this.revoked = true;
  }

  public void markReplacedBy(UUID replacedBy) {
    this.revoke();
    this.replacedBy = replacedBy;
  }

  private static void validateUserId(UUID userId) {
    if (userId == null) {
      throw new DomainException("El refresh token debe estar asociado a un usuario");
    }
  }

  private static void validateTokenHash(String tokenHash) {
    if (tokenHash == null || tokenHash.isBlank()) {
      throw new DomainException("El hash del refresh token no puede estar vacío");
    }
  }

  private static void validateExpiresAt(Instant expiresAt) {
    if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
      throw new DomainException("La fecha de expiración del refresh token debe ser futura");
    }
  }
}
