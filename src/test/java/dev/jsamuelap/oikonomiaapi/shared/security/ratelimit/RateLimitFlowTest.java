package dev.jsamuelap.oikonomiaapi.shared.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtService;
import dev.jsamuelap.oikonomiaapi.support.IntegrationTestSupport;

@TestPropertySource(properties = {"app.rate-limit.enabled=true", "app.rate-limit.login.capacity=2",
  "app.rate-limit.login.refill-tokens=1", "app.rate-limit.login.refill-period=1h", "app.rate-limit.signup.capacity=2",
  "app.rate-limit.signup.refill-tokens=1", "app.rate-limit.signup.refill-period=1h",
  "app.rate-limit.refresh.capacity=2", "app.rate-limit.refresh.refill-tokens=1",
  "app.rate-limit.refresh.refill-period=1h", "app.rate-limit.api.capacity=2", "app.rate-limit.api.refill-tokens=1",
  "app.rate-limit.api.refill-period=1h"})
@DisplayName("Rate limit flow")
public class RateLimitFlowTest extends IntegrationTestSupport {
  private static final String ORIGIN = "http://localhost:4200";

  @Autowired
  private RestTestClient client;

  @Autowired
  private JwtService jwtService;

  @Test
  @DisplayName("Should reject login after the burst with Retry-After and CORS headers")
  void shouldRejectLoginAfterTheBurstWithRetryAfterAndCorsHeaders() {
    attemptLogin().expectStatus().isUnauthorized().expectHeader().valueEquals("RateLimit-Limit", "2").expectHeader()
      .valueEquals("RateLimit-Remaining", "1");
    attemptLogin().expectStatus().isUnauthorized().expectHeader().valueEquals("RateLimit-Remaining", "0");

    attemptLogin().expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS).expectHeader().exists(HttpHeaders.RETRY_AFTER)
      .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).expectHeader()
      .valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN).expectHeader()
      .value(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
        exposed -> assertThat(exposed).contains("Retry-After", "RateLimit-Remaining"));
  }

  @Test
  @DisplayName("Should not consume tokens with CORS preflight")
  void shouldNotConsumeTokensWithCorsPreflight() {
    for (int i = 0; i < 5; i++) {
      client.options().uri("/api/v1/auth/refresh").header(HttpHeaders.ORIGIN, ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST").exchange().expectStatus().isOk();
    }

    client.post().uri("/api/v1/auth/refresh").exchange().expectStatus().isUnauthorized();
  }

  @Test
  @DisplayName("Should count API requests per user even from the same IP")
  void shouldCountApiRequestsPerUserEvenFromTheSameIp() {
    String userA = accessTokenOf(insertUser());
    String userB = accessTokenOf(insertUser());

    me(userA).expectStatus().isOk();
    me(userA).expectStatus().isOk();
    me(userA).expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

    me(userB).expectStatus().isOk();
  }

  private RestTestClient.ResponseSpec attemptLogin() {
    return client.post().uri("/api/v1/auth/login").header(HttpHeaders.ORIGIN, ORIGIN)
      .contentType(MediaType.APPLICATION_JSON).body(Map.of("email", "nobody@example.com", "password", "Wrong123!"))
      .exchange();
  }

  private RestTestClient.ResponseSpec me(String accessToken) {
    return client.get().uri("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchange();
  }

  private String accessTokenOf(UUID userId) {
    return jwtService.generate(userId.toString(), Map.of("email", "user@example.com", "type", "access"),
      Duration.ofMinutes(5));
  }
}
