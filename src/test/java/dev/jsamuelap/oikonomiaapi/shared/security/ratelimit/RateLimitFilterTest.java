package dev.jsamuelap.oikonomiaapi.shared.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.AuthenticatedPrincipal;

@DisplayName("Rate limit filter")
public class RateLimitFilterTest {
  private static final String IP = "10.0.0.1";
  private static final String LOGIN = "/api/v1/auth/login";
  private static final String SIGNUP = "/api/v1/auth/signup";
  private static final String REFRESH = "/api/v1/auth/refresh";
  private static final String API = "/api/v1/transactions";

  private static final RateLimitProperties PROPERTIES = new RateLimitProperties(true,
    new RateLimitProperties.Policy(2, 1, Duration.ofMinutes(10)),
    new RateLimitProperties.Policy(3, 2, Duration.ofHours(1)),
    new RateLimitProperties.Policy(4, 2, Duration.ofMinutes(1)),
    new RateLimitProperties.Policy(5, 1, Duration.ofMinutes(1)));

  private final AtomicInteger chainCalls = new AtomicInteger();
  private final RateLimitFilter filter = new RateLimitFilter(PROPERTIES, new JacksonJsonHttpMessageConverter());

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("Rate limit policies")
  class Policies {
    @ParameterizedTest
    @CsvSource({"POST, /api/v1/auth/login, 2", "POST, /api/v1/auth/signup, 3", "POST, /api/v1/auth/refresh, 4",
      "GET, /api/v1/auth/transactions, 5"})
    @DisplayName("Should apply the capacity of each policy")
    void shouldApplyTheCapacityOfEachPolicy(String method, String path, int capacity) throws Exception {
      for (int i = 0; i < capacity; i++) {
        assertThat(send(method, path, IP).getStatus()).as("request %d", i + 1).isEqualTo(200);
      }
      assertThat(send(method, path, IP).getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("Should keep policies independent for the same client")
    void shouldKeepPoliciesIndependentForTheSameClient() throws Exception {
      exhaust("POST", LOGIN, IP, 3);

      assertThat(post(SIGNUP).getStatus()).isEqualTo(200);
      assertThat(send("GET", API, IP).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should apply the strict login policy only to POST")
    void shouldApplyTheStrictLoginPolicyOnlyToPost() throws Exception {
      for (int i = 0; i < 3; i++) {
        assertThat(send("GET", LOGIN, IP).getStatus()).isEqualTo(200);
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/swagger-ui/index.html", "/api-docs", "/actuator/health"})
    @DisplayName("Should not limit routes outside the API")
    void shouldNotLimitRoutesOutsideTheApi(String path) throws Exception {
      for (int i = 0; i < 20; i++) {
        MockHttpServletResponse response = send("GET", path, IP);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("RateLimit-Limit")).isNull();
      }
    }

    @Test
    @DisplayName("Should not consume tokens on CORS preflight")
    void shouldNotConsumeTokensOnCorsPreflight() throws Exception {
      for (int i = 0; i < 10; i++) {
        MockHttpServletResponse response = send("OPTIONS", LOGIN, IP);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("RateLimit-Limit")).isNull();
      }

      assertThat(post(LOGIN).getStatus()).isEqualTo(200);
      assertThat(post(LOGIN).getStatus()).isEqualTo(200);
    }
  }

  @Nested
  @DisplayName("Rate limit keys")
  class Keys {
    @Test
    @DisplayName("Should separate anonymous clients by IP")
    void shouldSeparateAnonymousClientsByIp() throws Exception {
      exhaust("POST", LOGIN, "10.0.0.1", 2);

      assertThat(send("POST", LOGIN, "10.0.0.1").getStatus()).isEqualTo(429);
      assertThat(send("POST", LOGIN, "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should count API requests per user when authenticated")
    void shouldCountApiRequestsPerUserWhenAuthenticated() throws Exception {
      authenticateAs(UUID.randomUUID());
      exhaust("GET", API, IP, 5);
      assertThat(send("GET", API, IP).getStatus()).isEqualTo(429);

      authenticateAs(UUID.randomUUID()); // otro usuario pero con la misma IP
      assertThat(send("GET", API, IP).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should fall back to IP for API when there is no session")
    void shouldFallBackToIpForApiWhenThereIsNoSession() throws Exception {
      exhaust("GET", API, "10.0.0.1", 5);

      assertThat(send("GET", API, "10.0.0.1").getStatus()).isEqualTo(429);
      assertThat(send("GET", API, "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should key authentication routes by IP even when authenticated")
    void shouldKeyAuthenticationRoutesByIpEvenWhenAuthenticated() throws Exception {
      authenticateAs(UUID.randomUUID());
      exhaust("POST", LOGIN, IP, 2);

      authenticateAs(UUID.randomUUID());
      assertThat(post(LOGIN).getStatus()).isEqualTo(429);
    }

    @Nested
    @DisplayName("Rejected requests")
    class Rejection {
      @Test
      @DisplayName("Should respond with problem JSON and no internal details")
      void shouldRespondWithProblemJsonAndNoInternalDetails() throws Exception {
        exhaust("POST", LOGIN, IP, 2);

        MockHttpServletResponse response = post(LOGIN);
        String body = response.getContentAsString(StandardCharsets.UTF_8);

        SoftAssertions.assertSoftly(softly -> {
          softly.assertThat(response.getStatus()).as("status").isEqualTo(429);
          softly.assertThat(response.getContentType()).as("content type").startsWith("application/problem+json");
          softly.assertThat(body).as("body").contains("429").contains("Demasiadas solicitudes").doesNotContain("login");
        });
      }

      @ParameterizedTest
      @CsvSource({"POST, /api/v1/auth/login, 2, 600", "POST, /api/v1/auth/signup, 3, 1800",
        "POST, /api/v1/auth/refresh, 4, 30", "GET, /api/v1/transactions, 5, 60"})
      @DisplayName("Should announce the wait implied by the refill rate")
      void shouldAnnounceTheWaitImpliedByTheRefillRate(String method, String path, int capacity, long expectedSeconds)
        throws Exception {
        exhaust(method, path, IP, capacity);

        MockHttpServletResponse rejected = send(method, path, IP);

        assertThat(Long.parseLong(rejected.getHeader(HttpHeaders.RETRY_AFTER))).isBetween(expectedSeconds - 1,
          expectedSeconds + 1);
      }

      @Test
      @DisplayName("Should expose limit and remaining on every response")
      void shouldExposeLimitAndRemainingOnEveryResponse() throws Exception {
        MockHttpServletResponse first = post(LOGIN);
        MockHttpServletResponse second = post(LOGIN);
        MockHttpServletResponse rejected = post(LOGIN);

        SoftAssertions.assertSoftly(softly -> {
          softly.assertThat(first.getHeader("RateLimit-Limit")).as("limit").isEqualTo("2");
          softly.assertThat(first.getHeader("RateLimit-Remaining")).as("first").isEqualTo("1");
          softly.assertThat(second.getHeader("RateLimit-Remaining")).as("second").isEqualTo("0");
          softly.assertThat(rejected.getHeader("RateLimit-Remaining")).as("rejected").isEqualTo("0");
        });
      }

      @Test
      @DisplayName("Should not invoke the chain when rejected")
      void shouldNotInvokeTheChainWhenRejected() throws Exception {
        exhaust("POST", LOGIN, IP, 4);

        assertThat(chainCalls.get()).isEqualTo(2);
      }
    }
  }

  private MockHttpServletResponse send(String method, String path, String ip) throws Exception {
    var request = new MockHttpServletRequest(method, path);
    request.setRemoteAddr(ip);
    var response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> chainCalls.incrementAndGet());
    return response;
  }

  private MockHttpServletResponse post(String path) throws Exception {
    return send("POST", path, IP);
  }

  private void exhaust(String method, String path, String ip, int times) throws Exception {
    for (int i = 0; i < times; i++) {
      send(method, path, ip);
    }
  }

  private void authenticateAs(UUID userId) {
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(new AuthenticatedPrincipal(userId, "user@example.com"), null, List.of()));
  }
}
