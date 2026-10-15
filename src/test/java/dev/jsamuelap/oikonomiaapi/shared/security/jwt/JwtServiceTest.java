package dev.jsamuelap.oikonomiaapi.shared.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

@DisplayName("JWT service")
public class JwtServiceTest {
  private static final String SECRET = "test-secret-key-solo-pruebas-minimo-32-bytes-1234567890";
  private static final String OTHER_SECRET = "otro-secret-distinto-para-pruebas-minimo-32-bytes-0987654321";
  private static final Duration TTL = Duration.ofMinutes(15);

  private final JwtService jwtService = serviceWith(SECRET);

  private static JwtService serviceWith(String secret) {
    return new JwtService(new JwtProperties(secret, TTL, Duration.ofDays(30)));
  }

  @Nested
  @DisplayName("JWT service construction")
  class Construction {
    @ParameterizedTest
    @DisplayName("Should reject missing secret")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectMissingSecret(String secret) {
      assertThatThrownBy(() -> serviceWith(secret)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no está configurado");
    }

    @Test
    @DisplayName("Should reject secret shorter than 32 bytes")
    void shouldRejectSecretShorterThan32Bytes() {
      assertThatThrownBy(() -> serviceWith("a".repeat(31))).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("demasiado corto");
    }

    @Test
    @DisplayName("Should accept secret of exactly 32 bytes")
    void shouldAcceptSecretOfExactly32Bytes() {
      assertThatCode(() -> serviceWith("a".repeat(32))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should measure secret in bytes not characters")
    void shouldMeasureSecretInBytesNotCharacters() {
      assertThatCode(() -> serviceWith("ñ".repeat(16))).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("Token generation")
  class Generation {
    @Test
    @DisplayName("Should include subject, custom claims and TTL")
    void shouldIncludeSubjectCustomClaimsAndTtl() {
      String token = jwtService.generate("user-1", Map.of("email", "sam@example.com", "type", "access"), TTL);
      Claims claims = jwtService.parse(token);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(claims.getSubject()).as("subject").isEqualTo("user-1");
        softly.assertThat(claims.get("email", String.class)).as("email").isEqualTo("sam@example.com");
        softly.assertThat(claims.get("type", String.class)).as("type").isEqualTo("access");
        softly.assertThat(claims.getId()).as("jti").isNotBlank();
        softly.assertThat(Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()))
          .as("ttl").isEqualTo(TTL);
      });
    }

    @Test
    @DisplayName("Should generate different tokens for same input")
    void shouldGenerateDifferentTokensForSameInput() {
      String first = jwtService.generate("user-1", Map.of(), TTL);
      String second = jwtService.generate("user-1", Map.of(), TTL);

      assertThat(first).isNotEqualTo(second);
    }
  }

  @Nested
  @DisplayName("Token parsing")
  class Parsing {
    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
      String expired = jwtService.generate("user-1", Map.of(), Duration.ofMinutes(-1));

      assertThatThrownBy(() -> jwtService.parse(expired)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should reject token signed with another secret")
    void shouldRejectTokenSignedWithAnotherSecret() {
      String foreign = serviceWith(OTHER_SECRET).generate("user-1", Map.of(), TTL);

      assertThatThrownBy(() -> jwtService.parse(foreign)).isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should reject modified payload")
    void shouldRejectModifiedPayload() {
      String[] original = jwtService.generate("user-1", Map.of(), TTL).split("\\.");
      String[] other = jwtService.generate("user-2", Map.of(), TTL).split("\\.");
      String forged = other[0] + "." + other[1] + "." + original[2];

      assertThatThrownBy(() -> jwtService.parse(forged)).isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should reject unsigned token")
    void shouldRejectUnsignedToken() {
      String unsigned = Jwts.builder().subject("user-1").compact();

      assertThatThrownBy(() -> jwtService.parse(unsigned)).isInstanceOf(JwtException.class);
    }

    @ParameterizedTest
    @DisplayName("Should reject blank token")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectBlankToken(String token) {
      assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(IllegalArgumentException.class);
    }
  }
}
