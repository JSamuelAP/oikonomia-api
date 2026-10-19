package dev.jsamuelap.oikonomiaapi.shared.security.jwt;

import java.time.Duration;

import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(
  // spotless:off
  String secret,

  @NotNull
  @DurationMin(seconds = 1)
  Duration accessTokenTtl,

  @NotNull
  @DurationMin(seconds = 1)
  Duration refreshTokenTtl
  // spotless:on
) {
}
