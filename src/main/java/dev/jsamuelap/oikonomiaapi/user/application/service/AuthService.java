package dev.jsamuelap.oikonomiaapi.user.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.AuthenticationException;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.ConflictException;
import dev.jsamuelap.oikonomiaapi.user.domain.model.RefreshToken;
import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticationResult;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.LogoutUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RefreshTokenUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.PasswordEncoderPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.RefreshTokenRepositoryPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenGeneratorPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenHasherPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements RegisterUserUseCase, AuthenticateUserUseCase, RefreshTokenUseCase, LogoutUseCase {
  private final UserRepositoryPort userRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final TokenGeneratorPort tokenGenerator;
  private final TokenHasherPort tokenHasher;
  private final RefreshTokenRepositoryPort refreshTokenRepository;

  public static final String INVALID_REFRESH_TOKEN_MESSAGE = "Refresh token inválido";

  @Override
  @Transactional
  public UUID registerUser(RegisterUserCommand command) {
    if (userRepository.existsByEmail(command.email())) {
      throw new ConflictException("Ya existe un usuario con este email");
    }

    String hashedPassword = passwordEncoder.encode(command.rawPassword());
    User user = User.register(command.firstName(), command.lastName(), command.email(), hashedPassword);
    User saved = userRepository.save(user);

    return saved.getId();
  }

  @Override
  @Transactional()
  public AuthenticationResult authenticate(AuthenticateUserCommand command) {
    User user = userRepository.findByEmail(command.email())
      .orElseThrow(() -> new AuthenticationException("Email o contraseña incorrectos"));

    if (!passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
      throw new AuthenticationException("Incorrect password");
    }

    String accessToken = tokenGenerator.generateAccessToken(user.getId(), user.getEmail());
    String rawRefreshToken = tokenGenerator.generateRefreshToken(user.getId());
    persistRefreshToken(user.getId(), rawRefreshToken);

    return new AuthenticationResult(accessToken, rawRefreshToken);
  }

  @Override
  @Transactional
  public AuthenticationResult refresh(String rawRefreshToken) {
    String presentedHash = tokenHasher.hash(rawRefreshToken);

    RefreshToken storedToken = refreshTokenRepository.findByTokenHash(presentedHash)
      .orElseThrow(() -> new AuthenticationException(INVALID_REFRESH_TOKEN_MESSAGE));

    if (storedToken.isRevoked()) {
      // Alguien está reusando un token que ya fue rotado o expirado, significa que
      // fue robado
      refreshTokenRepository.revokeAllByUserId(storedToken.getUserId());
      throw new AuthenticationException(INVALID_REFRESH_TOKEN_MESSAGE);
    }

    if (storedToken.isExpired()) {
      throw new AuthenticationException(INVALID_REFRESH_TOKEN_MESSAGE);
    }

    User user = userRepository.findById(storedToken.getUserId())
      .orElseThrow(() -> new AuthenticationException(INVALID_REFRESH_TOKEN_MESSAGE));

    // Guardar nuevo refresh token
    String newRawRefreshToken = tokenGenerator.generateRefreshToken(user.getId());
    String newTokenHash = tokenHasher.hash(newRawRefreshToken);
    Instant newExpiresAt = Instant.now().plus(tokenGenerator.refreshTokenTtl());
    RefreshToken newToken = RefreshToken.issue(user.getId(), newTokenHash, newExpiresAt);
    RefreshToken savedNewToken = refreshTokenRepository.save(newToken);

    // Rotar refresh token anterior
    storedToken.markReplacedBy(savedNewToken.getId());
    refreshTokenRepository.save(storedToken);

    String newAccessToken = tokenGenerator.generateAccessToken(user.getId(), user.getEmail());

    return new AuthenticationResult(newAccessToken, newRawRefreshToken);
  }

  @Override
  @Transactional
  public void logout(UUID userId) {
    refreshTokenRepository.revokeAllByUserId(userId);
  }

  private void persistRefreshToken(UUID userId, String rawRefreshToken) {
    String tokenHash = tokenHasher.hash(rawRefreshToken);
    Instant expiresAt = Instant.now().plus(tokenGenerator.refreshTokenTtl());

    RefreshToken refreshToken = RefreshToken.issue(userId, tokenHash, expiresAt);
    refreshTokenRepository.save(refreshToken);
  }
}
