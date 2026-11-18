package dev.jsamuelap.oikonomiaapi.shared.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Rate limit properties")
public class RateLimitPropertiesTest {
  private static final RateLimitProperties.Policy VALID = new RateLimitProperties.Policy(1, 1, Duration.ofMinutes(1));

  @Test
  @DisplayName("Should allow missing policies when disabled")
  void shouldAllowMissingPoliciesWhenDisabled() {
    assertThatCode(() -> new RateLimitProperties(false, null, null, null, null)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"login", "signup", "refresh", "api"})
  @DisplayName("Should require every policy when enabled")
  void shouldRequireEveryPolicyWhenEnabled(String missing) {
    assertThatThrownBy(() -> new RateLimitProperties(true, unlessMissing(missing, "login"),
      unlessMissing(missing, "signup"), unlessMissing(missing, "refresh"), unlessMissing(missing, "api")))
      .isInstanceOf(NullPointerException.class).hasMessageContaining(missing);
  }

  @ParameterizedTest
  @CsvSource({"0, 1, PT1M", "-1, 1, PT1M", "1, 0, PT1M", "1, -1, PT1M", "1, 1, PT0S", "1, 1, -PT1M"})
  @DisplayName("Should reject non-positive capacity, refill tokens and period")
  void shouldRejectNonPositiveValues(int capacity, int refillTokens, Duration period) {
    assertThatThrownBy(() -> new RateLimitProperties.Policy(capacity, refillTokens, period))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Should reject missing period")
  void shouldRejectMissingPeriod() {
    assertThatThrownBy(() -> new RateLimitProperties.Policy(1, 1, null)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Should accept the smallest valid policy")
  void shouldAcceptTheSmallestValidPolicy() {
    assertThatCode(() -> new RateLimitProperties.Policy(1, 1, Duration.ofNanos(1))).doesNotThrowAnyException();
  }

  private static RateLimitProperties.Policy unlessMissing(String missing, String name) {
    return name.equals(missing) ? null : VALID;
  }
}
