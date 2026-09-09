package dev.jsamuelap.oikonomiaapi.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.DomainException;

@DisplayName("Refresh token domain model")
public class RefreshTokenTest {
  private static final UUID VALID_USER_ID = UUID.randomUUID();
  private static final String VALID_TOKEN_HASH = "hash123";
  private static final Instant VALID_EXPIRES_AT = Instant.now().plus(Duration.ofDays(1));

  @Nested
  @DisplayName("Refresh token issuance")
  class Issue {
    @Test
    @DisplayName("Should issue a valid refresh token with all fields")
    void shouldIssueValidRefreshToken() {
      RefreshToken token = RefreshToken.issue(VALID_USER_ID, VALID_TOKEN_HASH, VALID_EXPIRES_AT);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(token).as("token").isNotNull();
        softly.assertThat(token.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(token.getTokenHash()).as("tokenHash").isEqualTo(VALID_TOKEN_HASH);
        softly.assertThat(token.getExpiresAt()).as("expiresAt").isEqualTo(VALID_EXPIRES_AT);
        softly.assertThat(token.getCreatedAt()).as("createdAt").isNotNull();
        softly.assertThat(token.getReplacedBy()).as("replacedBy").isNull();
        softly.assertThat(token.isValid()).as("isValid").isTrue();
      });
    }

    @Test
    @DisplayName("Should reject null user id")
    void shouldRejectNullUserId() {
      assertThatThrownBy(() -> RefreshToken.issue(null, VALID_TOKEN_HASH, VALID_EXPIRES_AT))
        .isInstanceOf(DomainException.class).hasMessageContaining("debe estar asociado a un usuario");
    }

    @ParameterizedTest(name = "should reject: {0}")
    @DisplayName("Should reject blank token hash")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankTokenHash(String invalidTokenHash) {
      assertThatThrownBy(() -> RefreshToken.issue(VALID_USER_ID, invalidTokenHash, VALID_EXPIRES_AT))
        .isInstanceOf(DomainException.class).hasMessageContaining("no puede estar vacío");
    }

    @Nested
    @DisplayName("Expires at validation")
    class ExpiresAt {
      @Test
      @DisplayName("Should reject null expires at")
      void shouldRejectNullExpiresAt() {
        assertThatThrownBy(() -> RefreshToken.issue(VALID_USER_ID, VALID_TOKEN_HASH, null))
          .isInstanceOf(DomainException.class).hasMessageContaining("es requerida");
      }

      @Test
      @DisplayName("Should reject expires at in the past")
      void shouldRejectExpiresAtInThePast() {
        Instant past = Instant.now().minus(Duration.ofDays(1));
        assertThatThrownBy(() -> RefreshToken.issue(VALID_USER_ID, VALID_TOKEN_HASH, past))
          .isInstanceOf(DomainException.class).hasMessageContaining("debe ser futura");
      }

      @Test
      @DisplayName("Should accept expires at in the future")
      void shouldAcceptExpiresAtInTheFuture() {
        Instant future = Instant.now().plus(Duration.ofDays(15));
        RefreshToken token = RefreshToken.issue(VALID_USER_ID, VALID_TOKEN_HASH, future);
        assertThat(token.getExpiresAt()).isEqualTo(future);
      }
    }
  }

  @Nested
  @DisplayName("Refresh token reconstitution from persistence")
  class Reconstitution {
    @Test
    @DisplayName("Should reconstitute refresh token without re-validating fields")
    void shouldReconstituteWithoutRevalidating() {
      RefreshToken token = RefreshToken.reconstitute(null, null, "", null, true, null, null);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(token).as("token").isNotNull();
        softly.assertThat(token.getId()).as("id").isNull();
        softly.assertThat(token.getUserId()).as("userId").isNull();
        softly.assertThat(token.getTokenHash()).as("tokenHash").isBlank();
        softly.assertThat(token.getExpiresAt()).as("expiresAt").isNull();
        softly.assertThat(token.isRevoked()).as("revoked").isTrue();
        softly.assertThat(token.getCreatedAt()).as("createdAt").isNull();
        softly.assertThat(token.getReplacedBy()).as("replacedBy").isNull();
      });
    }

    @Test
    @DisplayName("Should reconstitute refresh token with all fields")
    void shouldReconstituteWithAllFields() {
      UUID id = UUID.randomUUID();
      Instant createdAt = Instant.now();
      UUID replacedBy = UUID.randomUUID();
      RefreshToken token = RefreshToken.reconstitute(id, VALID_USER_ID, VALID_TOKEN_HASH, VALID_EXPIRES_AT, true,
        createdAt, replacedBy);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(token).as("token").isNotNull();
        softly.assertThat(token.getId()).as("id").isEqualTo(id);
        softly.assertThat(token.getUserId()).as("userId").isEqualTo(VALID_USER_ID);
        softly.assertThat(token.getTokenHash()).as("tokenHash").isEqualTo(VALID_TOKEN_HASH);
        softly.assertThat(token.getExpiresAt()).as("expiresAt").isEqualTo(VALID_EXPIRES_AT);
        softly.assertThat(token.isRevoked()).as("revoked").isTrue();
        softly.assertThat(token.getCreatedAt()).as("createdAt").isEqualTo(createdAt);
        softly.assertThat(token.getReplacedBy()).as("replacedBy").isEqualTo(replacedBy);
      });
    }
  }

  @Test
  @DisplayName("Should mark as replaced")
  void shouldMarkAsReplaced() {
    RefreshToken token = RefreshToken.issue(VALID_USER_ID, VALID_TOKEN_HASH, VALID_EXPIRES_AT);
    UUID newTokenId = UUID.randomUUID();
    token.markReplacedBy(newTokenId);

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(token.isRevoked()).as("revoked").isTrue();
      softly.assertThat(token.getReplacedBy()).as("replacedBy").isEqualTo(newTokenId);
    });
  }
}
