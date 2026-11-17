package dev.jsamuelap.oikonomiaapi.shared.security.ratelimit;

import java.time.Duration;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(boolean enabled, Policy login, Policy signup, Policy refresh, Policy api) {
  public RateLimitProperties {
    if (enabled) {
      Objects.requireNonNull(login, "Falta app.rate-limit.login");
      Objects.requireNonNull(signup, "Falta app.rate-limit.signup");
      Objects.requireNonNull(refresh, "Falta app.rate-limit.refresh");
      Objects.requireNonNull(api, "Falta app.rate-limit.api");
    }
  }

  public record Policy(int capacity, int refillTokens, Duration refillPeriod) {
    public Policy {
      if (capacity < 1 || refillTokens < 1 || refillPeriod == null || refillPeriod.isZero()
        || refillPeriod.isNegative()) {
        throw new IllegalStateException("Límite de tasa inválido: los valores deben ser positivos");
      }
    }
  }

  Policy limitsFor(RateLimitPolicy policy) {
    return switch (policy) {
      case LOGIN -> login;
      case SIGNUP -> signup;
      case REFRESH -> refresh;
      case API -> api;
    };
  }
}
