package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import dev.jsamuelap.oikonomiaapi.support.IntegrationTestSupport;
import dev.jsamuelap.oikonomiaapi.user.domain.model.RefreshToken;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.RefreshTokenRepositoryPort;

@DisplayName("Refresh token repository adapter")
public class RefreshTokenRepositoryAdapterTest extends IntegrationTestSupport {
  @Autowired
  private RefreshTokenRepositoryPort refreshTokenRepository;

  @Nested
  @DisplayName("Save refresh token")
  class Save {
    @Test
    @DisplayName("Should persist all fields")
    void shouldPersistAllFields() {
      UUID userId = insertUser();
      RefreshToken token = newToken(userId, "hash-1");
      refreshTokenRepository.save(token);

      RefreshToken found = refreshTokenRepository.findByTokenHash("hash-1").orElseThrow();
      assertThat(found).usingRecursiveComparison().ignoringFields("createdAt", "expiresAt").isEqualTo(token);
      assertThat(found.getExpiresAt()).isCloseTo(token.getExpiresAt(), within(1, ChronoUnit.MILLIS));
      assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should reject duplicate token hash")
    void shouldRejectDuplicateTokenHash() {
      UUID userId = insertUser();
      refreshTokenRepository.save(newToken(userId, "same-hash"));

      assertThatThrownBy(() -> refreshTokenRepository.save(newToken(userId, "same-hash")))
        .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should persist revocation")
    void shouldPersistRevocation() {
      UUID userId = insertUser();
      RefreshToken token = newToken(userId, "hash-1");
      refreshTokenRepository.save(token);

      token.revoke();
      refreshTokenRepository.save(token);

      assertThat(refreshTokenRepository.findByTokenHash("hash-1").orElseThrow().isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should rotate token within a single transaction")
    void shouldRotateTokenWithinASingleTransaction() {
      UUID userId = insertUser();
      refreshTokenRepository.save(newToken(userId, "old-hash"));

      transactionTemplate.executeWithoutResult(status -> {
        RefreshToken loaded = refreshTokenRepository.findByTokenHash("old-hash").orElseThrow();
        RefreshToken issued = refreshTokenRepository.save(newToken(userId, "new-hash"));
        loaded.markReplacedBy(issued.getId());
        refreshTokenRepository.save(loaded);
      });

      RefreshToken old = refreshTokenRepository.findByTokenHash("old-hash").orElseThrow();
      RefreshToken replacement = refreshTokenRepository.findByTokenHash("new-hash").orElseThrow();
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(old.isRevoked()).as("old revoked").isTrue();
        softly.assertThat(old.getReplacedBy()).as("old replacedBy").isEqualTo(replacement.getId());
        softly.assertThat(replacement.isRevoked()).as("new revoked").isFalse();
      });
    }
  }

  @Nested
  @DisplayName("Find by token hash")
  class FindByTokenHash {
    @Test
    @DisplayName("Should return empty when hash does not exist")
    void shouldReturnEmptyWhenHashDoesNotExist() {
      assertThat(refreshTokenRepository.findByTokenHash("unknown")).isEmpty();
    }
  }

  @Nested
  @DisplayName("Revoke all by user ID")
  class RevokeAllByUserId {
    @Test
    @DisplayName("Should revoke only tokens of the given user")
    void shouldRevokeOnlyTokensOfTheGivenUser() {
      UUID userA = insertUser();
      UUID userB = insertUser();
      refreshTokenRepository.save(newToken(userA, "a-1"));
      refreshTokenRepository.save(newToken(userA, "a-2"));
      refreshTokenRepository.save(newToken(userB, "b-1"));

      // Métodos @Modifying requieren una transacción, no abren una por si mismos
      // En producción la aporta @Transacional, en test debe ser con
      // transactionTemplate
      transactionTemplate.executeWithoutResult(status -> refreshTokenRepository.revokeAllByUserId(userA));

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(refreshTokenRepository.findByTokenHash("a-1").orElseThrow().isRevoked()).as("a-1").isTrue();
        softly.assertThat(refreshTokenRepository.findByTokenHash("a-2").orElseThrow().isRevoked()).as("a-2").isTrue();
        softly.assertThat(refreshTokenRepository.findByTokenHash("b-1").orElseThrow().isRevoked()).as("b-1").isFalse();
      });
    }
  }

  private RefreshToken newToken(UUID userId, String hash) {
    return RefreshToken.issue(userId, hash, Instant.now().plus(Duration.ofDays(30)));
  }
}
