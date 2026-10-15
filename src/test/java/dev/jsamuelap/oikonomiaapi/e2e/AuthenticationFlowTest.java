package dev.jsamuelap.oikonomiaapi.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import dev.jsamuelap.oikonomiaapi.shared.security.jwt.JwtCookies;
import dev.jsamuelap.oikonomiaapi.support.IntegrationTestSupport;
import dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest.LoginResponse;

@DisplayName("Authentication flow E2E tests")
public class AuthenticationFlowTest extends IntegrationTestSupport {
  @Autowired
  private RestTestClient client;

  private static final String PASSWORD = "Password123#";

  private record Session(String accessToken, String refreshToken) {
  }

  @Test
  @DisplayName("Should login with email in different case and surrounding spaces")
  void shouldLoginWithEmailInDifferentCaseAndSurroundingSpaces() {
    signup("sam@example.com");

    assertThatCode(() -> login("  SAM@Example.com  ")).doesNotThrowAnyException();
  }

  @Nested
  @DisplayName("Protected routes")
  class ProtectedRoutes {
    @Test
    @DisplayName("Should reject protected route without token")
    void shouldRejectProtectedRouteWithoutToken() {
      client.get().uri("/api/v1/users/me").exchange().expectStatus().isUnauthorized().expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @DisplayName("Should accept valid access token")
    void shouldAcceptValidAccessToken() {
      Session session = signupAndLogin("sam@example.com");

      client.get().uri("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + session.accessToken())
        .exchange().expectStatus().isOk().expectBody().jsonPath("$.email").isEqualTo("sam@example.com");
    }

    @Test
    @DisplayName("Should reject refresh token used as bearer")
    void shouldRejectRefreshTokenUsedAsBearer() {
      Session session = signupAndLogin("sam@example.com");

      client.get().uri("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + session.refreshToken())
        .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject tampered signature")
    void shouldRejectTamperedSignature() {
      Session session = signupAndLogin("sam@example.com");
      String[] parts = session.accessToken().split("\\.");
      String tampered = parts[0] + "." + parts[1] + "A".repeat(43);

      client.get().uri("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered).exchange()
        .expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @DisplayName("Should reject malformed token")
    @ValueSource(strings = {"not-a-jwt", "a.b.c"})
    void shouldRejectMalformedToken(String token) {
      client.get().uri("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange()
        .expectStatus().isUnauthorized();
    }
  }

  @Nested
  @DisplayName("Logout")
  class Logout {
    @Test
    @DisplayName("Should reject logout without token")
    void shouldRejectLogoutWithoutToken() {
      client.post().uri("/api/v1/auth/logout").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should revoke every session and expire cookie")
    void shouldRevokeEverySessionAndExpireCookie() {
      Session first = signupAndLogin("sam@example.com");
      login("sam@example.com");
      assertThat(activeRefreshToken()).isEqualTo(2);

      client.post().uri("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + first.accessToken())
        .exchange().expectStatus().isNoContent().expectHeader()
        .value(HttpHeaders.SET_COOKIE, cookie -> assertThat(cookie).contains("Max-Age=0"));

      assertThat(activeRefreshToken()).isZero();
    }

    @Test
    @DisplayName("Should reject refresh after logout")
    void shouldRejectRefreshAfterLogout() {
      Session session = signupAndLogin("sam@example.com");

      client.post().uri("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + session.accessToken())
        .exchange().expectStatus().isNoContent();

      client.post().uri("/api/v1/auth/refresh").header(HttpHeaders.AUTHORIZATION, "Bearer " + session.refreshToken())
        .exchange().expectStatus().isUnauthorized();
    }
  }

  @Nested
  @DisplayName("Login failures")
  class LoginFailures {
    @Test
    @DisplayName("Should return same response for wrong password and unknown email")
    void shouldReturnSameResponseForWrongPasswordAndUnknownEmail() {
      signup("sam@example.com");

      String wrongPassword = failedLoginBody("sam@example.com", "Wrong123#");
      String unknownEmail = failedLoginBody("nobody@example.com", PASSWORD);

      assertThat(wrongPassword).isEqualTo(unknownEmail);
    }

    @Test
    @DisplayName("Should not issue tokens on failed login")
    void shouldNotIssueTokensOnFailedLogin() {
      signup("sam@example.com");
      failedLoginBody("sam@example.com", "Wrong123#");

      assertThat(activeRefreshToken()).isZero();
    }
  }

  @Nested
  @DisplayName("Session contract")
  class SessionContract {
    @Test
    @DisplayName("Should set refresh cookie with secure attributes")
    void shouldSetRefreshCookieWithSecureAttributes() {
      signup("sam@example.com");

      client.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("email", "sam@example.com", "password", PASSWORD)).exchange().expectStatus().isOk().expectHeader()
        .value(HttpHeaders.SET_COOKIE, cookie -> {
          SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(cookie).as("name").startsWith("refresh_token=");
            softly.assertThat(cookie).as("HttpOnly").contains("HttpOnly");
            softly.assertThat(cookie).as("Secure").contains("Secure");
            softly.assertThat(cookie).as("SameSite").contains("SameSite=Strict");
            softly.assertThat(cookie).as("Path").contains("Path=/api/v1/auth");
            softly.assertThat(cookie).as("Max-Age").contains("Max-Age=2592000");
          });
        });
    }

    @Test
    @DisplayName("Should not expose refresh token in body")
    void shouldNotExposeRefreshTokenInBody() {
      signup("sam@example.com");

      var result = client.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("email", "sam@example.com", "password", PASSWORD)).exchange().expectStatus().isOk()
        .expectBody(String.class).returnResult();

      String refreshToken = cookieValue(result.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE));
      assertThat(result.getResponseBody()).doesNotContain(refreshToken).doesNotContain("refresh");
    }

    @Test
    @DisplayName("Should store only a hash of the refresh token")
    void shouldStoreOnlyAHashOfTheRefreshToken() {
      Session session = signupAndLogin("sam@example.com");

      String stored = jdbc.queryForObject("SELECT token_hash FROM refresh_tokens", String.class);

      assertThat(stored).matches("[0-9a-f]{64}").isNotEqualTo(session.refreshToken());
    }

    @Test
    @DisplayName("Should rotate token on refresh")
    void shouldRotateTokenOnRefresh() {
      Session first = signupAndLogin("sam@example.com");
      Session rotated = refresh(first.refreshToken());

      UUID replacedBy = jdbc.queryForObject("SELECT replaced_by FROM refresh_tokens WHERE revoked = true", UUID.class);
      UUID activeId = jdbc.queryForObject("SELECT id FROM refresh_tokens WHERE revoked = false", UUID.class);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(rotated.refreshToken()).as("new refresh token").isNotEqualTo(first.refreshToken());
        softly.assertThat(rotated.accessToken()).as("new access token").isNotEqualTo(first.accessToken());
        softly.assertThat(activeRefreshToken()).as("active tokens").isEqualTo(1);
        softly.assertThat(replacedBy).as("replaces_by").isEqualTo(activeId);
      });
    }
  }

  @Nested
  @DisplayName("Refresh failures")
  class RefreshFailures {
    @Test
    @DisplayName("Should reject refresh without cookie")
    void shouldRejectRefreshWithoutCookie() {
      client.post().uri("/api/v1/auth/refresh").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject unknown refresh token")
    void shouldRejectUnknownRefreshToken() {
      client.post().uri("/api/v1/auth/refresh").header(HttpHeaders.COOKIE, cookieHeader("garbage")).exchange()
        .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject access token sent as refresh cookie")
    void shouldRejectAccessTokenSentAsRefreshCookie() {
      Session session = signupAndLogin("sam@example.com");

      client.post().uri("/api/v1/auth/refresh").header(HttpHeaders.COOKIE, cookieHeader(session.accessToken()))
        .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should revoke all sessions where rotated token is reused")
    void shouldRevokeAllSessionsWhereRotatedTokenIsReused() {
      Session first = signupAndLogin("sam@example.com");
      Session rotated = refresh(first.refreshToken());

      client.post().uri("/api/v1/auth/refresh").header(HttpHeaders.COOKIE, cookieHeader(first.refreshToken()))
        .exchange().expectStatus().isUnauthorized();

      client.post().uri("/api/v1/auth/refresh").header(HttpHeaders.COOKIE, cookieHeader(rotated.refreshToken()))
        .exchange().expectStatus().isUnauthorized();

      assertThat(activeRefreshToken()).isZero();
    }
  }

  @Nested
  @DisplayName("Signup")
  class Signup {
    @Test
    @DisplayName("Should create user with bcrypt hash and no leak")
    void shouldCreateUserWithBcryptHashAndNoLeak() {
      var result = client.post().uri("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("firstName", "Sam", "lastName", "Aldana", "email", "sam@example.com", "password", PASSWORD))
        .exchange().expectStatus().isCreated().expectBody(String.class).returnResult();

      String hash = jdbc.queryForObject("SELECT password_hash FROM users WHERE email = 'sam@example.com'",
        String.class);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result.getResponseBody()).as("body").contains("\"userId\"").doesNotContain("password");
        softly.assertThat(hash).as("hash").startsWith("$2a$10$").isNotEqualTo(PASSWORD);
      });
    }

    @Test
    @DisplayName("Should reject duplicate email even with different case and spaces")
    void shouldRejectDuplicateEmailEvenWithDifferentCaseAndSpaces() {
      signup("sam@example.com");

      client.post().uri("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("firstName", "Manuel", "lastName", "Aldana", "email", "  SAM@Example.com ", "password", PASSWORD))
        .exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @ParameterizedTest
    @DisplayName("Should reject weak password")
    @ValueSource(strings = {"Ab1@", "password1@", "PASSWORD1@", "Password@@", "Password12"})
    void shouldRejectWeakPassword(String password) {
      client.post().uri("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("firstName", "Sam", "lastName", "Aldana", "email", "sam@example.com", "password", password))
        .exchange().expectStatus().isBadRequest();
    }
  }

  @Nested
  @DisplayName("Infrastructure")
  class Infrastructure {
    @Test
    @DisplayName("Should serve OpenAPI document")
    void shouldServeOpenApiDocument() {
      client.get().uri("/api-docs").exchange().expectStatus().isOk().expectBody()
        .jsonPath("$.paths['/api/v1/auth/login']").exists().jsonPath("$.components.securitySchemes.bearerAuth").exists()
        .jsonPath("$.paths['/api/v1/auth/login'].post.responses['401'].content['application/problem+json']").exists();
    }

    @Test
    @DisplayName("Should answer CORS preflight for logout")
    void shouldAnswerCorsPreflightForLogout() {
      client.options().uri("/api/v1/auth/logout").header(HttpHeaders.ORIGIN, "http://localhost:4200")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type").exchange().expectStatus()
        .isOk().expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200")
        .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    }
  }

  private Session signupAndLogin(String email) {
    signup(email);
    return login(email);
  }

  private Session login(String email) {
    var result = client.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
      .body(Map.of("email", email, "password", PASSWORD)).exchange().expectStatus().isOk()
      .expectBody(LoginResponse.class).returnResult();
    return toSession(result);
  }

  private void signup(String email) {
    client.post().uri("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
      .body(Map.of("firstName", "Sam", "lastName", "Aldana", "email", email, "password", PASSWORD)).exchange()
      .expectStatus().isCreated();
  }

  private Session refresh(String refreshToken) {
    var result = client.post().uri("/api/v1/auth/refresh").header(HttpHeaders.COOKIE, cookieHeader(refreshToken))
      .exchange().expectStatus().isOk().expectBody(LoginResponse.class).returnResult();
    return toSession(result);
  }

  private String failedLoginBody(String email, String password) {
    return client.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
      .body(Map.of("email", email, "password", password)).exchange().expectStatus().isUnauthorized()
      .expectBody(String.class).returnResult().getResponseBody();
  }

  private static Session toSession(EntityExchangeResult<LoginResponse> result) {
    String setCookie = result.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
    return new Session(result.getResponseBody().accessToken(), cookieValue(setCookie));
  }

  private static String cookieValue(String setCookie) {
    return setCookie.substring(setCookie.indexOf("=") + 1, setCookie.indexOf(";"));
  }

  private static String cookieHeader(String value) {
    return JwtCookies.REFRESH_TOKEN_COOKIE + "=" + value;
  }

  private int activeRefreshToken() {
    return jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE revoked = false", Integer.class);
  }
}
