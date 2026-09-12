package dev.jsamuelap.oikonomiaapi.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.AuthenticationException;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.ConflictException;
import dev.jsamuelap.oikonomiaapi.user.domain.model.RefreshToken;
import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticationResult;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.PasswordEncoderPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.RefreshTokenRepositoryPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenGeneratorPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenHasherPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth service")
class AuthServiceTest {
  @Mock
  private UserRepositoryPort userRepository;
  @Mock
  private PasswordEncoderPort passwordEncoder;
  @Mock
  private TokenGeneratorPort tokenGenerator;
  @Mock
  private TokenHasherPort tokenHasher;
  @Mock
  private RefreshTokenRepositoryPort refreshTokenRepository;

  private AuthService authService;

  public static final String USER_EMAIL = "sam@example.com";
  public static final String USER_RAW_PASSWORD = "Password123!";
  public static final String USER_HASHED_PASSWORD = "hashed-value";

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordEncoder, tokenGenerator, tokenHasher, refreshTokenRepository);
  }

  @Nested
  @DisplayName("User registration")
  class Register {
    @Test
    @DisplayName("Should reject registration when email already exists")
    void shouldRejectWhenEmailAlreadyExists() {
      when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

      var command = new RegisterUserCommand("Sam", "Aldana", USER_EMAIL, USER_RAW_PASSWORD);
      assertThatThrownBy(() -> authService.registerUser(command)).isInstanceOf(ConflictException.class);

      verify(userRepository, never()).save(any());
      verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Should register user and return user ID")
    void shouldRegisterAndReturnUserId() {
      when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(USER_RAW_PASSWORD)).thenReturn(USER_HASHED_PASSWORD);
      when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

      var command = new RegisterUserCommand("Sam", "Aldana", USER_EMAIL, USER_RAW_PASSWORD);
      UUID userId = authService.registerUser(command);

      assertThat(userId).isNotNull();

      verify(userRepository).save(argThat(user -> user.getPasswordHash().equals(USER_HASHED_PASSWORD)));
    }
  }

  @Nested
  @DisplayName("User authentication")
  class Authenticate {
    @Test
    @DisplayName("Should reject authentication when email does not exist")
    void shouldRejectWhenEmailDoesNotExists() {
      when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

      var command = new AuthenticateUserCommand(USER_EMAIL, USER_RAW_PASSWORD);
      assertThatThrownBy(() -> authService.authenticate(command)).isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Email o contraseña incorrectos");

      verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject authentication when password does not match")
    void shouldRejectWhenPasswordDoesNotMatch() {
      User user = User.reconstitute(UUID.randomUUID(), "Sam", "Aldana", USER_EMAIL, USER_HASHED_PASSWORD, null);
      when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(USER_RAW_PASSWORD, user.getPasswordHash())).thenReturn(false);

      var command = new AuthenticateUserCommand(USER_EMAIL, USER_RAW_PASSWORD);
      assertThatThrownBy(() -> authService.authenticate(command)).isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Email o contraseña incorrectos");

      verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should persist refresh token on successful authentication")
    void shouldPersistRefreshToken() {
      User user = User.reconstitute(UUID.randomUUID(), "Sam", "Aldana", USER_EMAIL, USER_HASHED_PASSWORD, null);
      when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(USER_RAW_PASSWORD, user.getPasswordHash())).thenReturn(true);
      when(tokenGenerator.generateAccessToken(any(), any())).thenReturn("raw-access-token");
      when(tokenGenerator.generateRefreshToken(any())).thenReturn("raw-refresh-token");
      when(tokenHasher.hash(any())).thenReturn("hashed-token");

      var command = new AuthenticateUserCommand(USER_EMAIL, USER_RAW_PASSWORD);
      AuthenticationResult result = authService.authenticate(command);

      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result).as("authenticationResult").isNotNull();
        softly.assertThat(result.accessToken()).as("accessToken").isEqualTo("raw-access-token");
        softly.assertThat(result.refreshToken()).as("refreshToken").isEqualTo("raw-refresh-token");
      });

      verify(refreshTokenRepository)
        .save(argThat(token -> token.getUserId().equals(user.getId()) && token.getTokenHash().equals("hashed-token")));
    }
  }

  @Nested
  @DisplayName("Token refresh")
  class Refresh {
    private final UUID userId = UUID.randomUUID();
    private final String rawToken = "raw-refresh-token";
    private final String tokenHash = "hashed-token";

    @Test
    @DisplayName("Should reject refresh when token not found")
    void shouldRejectWhenTokenNotFound() {
      when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);
      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh(rawToken)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("Should revoke all sessions when reused token detected")
    void shouldRevokeAllSessionsWhenReusedTokenDetected() {
      Instant expiresAt = Instant.now().plus(Duration.ofDays(1));
      RefreshToken revokedToken = RefreshToken.reconstitute(UUID.randomUUID(), userId, tokenHash, expiresAt, true,
        Instant.now(), UUID.randomUUID());

      when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);
      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revokedToken));

      assertThatThrownBy(() -> authService.refresh(rawToken)).isInstanceOf(AuthenticationException.class);

      verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    @DisplayName("Should reject refresh when token expired")
    void shouldRejectWhenTokenExpired() {
      Instant expiresAt = Instant.now().minus(Duration.ofDays(1));
      RefreshToken expiredToken = RefreshToken.reconstitute(UUID.randomUUID(), userId, tokenHash, expiresAt, false,
        Instant.now(), null);

      when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);
      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

      assertThatThrownBy(() -> authService.refresh(rawToken)).isInstanceOf(AuthenticationException.class);

      verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    @DisplayName("Should reject refresh when user no longer exists")
    void shouldRejectWhenUserNoLongerExists() {
      Instant expiresAt = Instant.now().plus(Duration.ofDays(1));
      RefreshToken validToken = RefreshToken.reconstitute(UUID.randomUUID(), userId, tokenHash, expiresAt, false,
        Instant.now(), null);

      when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);
      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(validToken));
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh(rawToken)).isInstanceOf(AuthenticationException.class);
    }
  }

  @Nested
  @DisplayName("User logout")
  class Logout {
    @Test
    @DisplayName("Should revoke all sessions on logout")
    void shouldRevokeAllSessions() {
      UUID userId = UUID.randomUUID();
      authService.logout(userId);
      verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
  }
}
