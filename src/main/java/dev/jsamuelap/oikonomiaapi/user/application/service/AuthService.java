package dev.jsamuelap.oikonomiaapi.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jsamuelap.oikonomiaapi.shared.domain.exception.AuthenticationException;
import dev.jsamuelap.oikonomiaapi.shared.domain.exception.ConflictException;
import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticationResult;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.LogoutUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserUseCase;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.PasswordEncoderPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.TokenGeneratorPort;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements RegisterUserUseCase, AuthenticateUserUseCase, LogoutUseCase {
  private final UserRepositoryPort userRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final TokenGeneratorPort tokenGenerator;

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
  @Transactional(readOnly = true)
  public AuthenticationResult authenticate(AuthenticateUserCommand command) {
    User user = userRepository.findByEmail(command.email())
      .orElseThrow(() -> new AuthenticationException("Email o contraseña incorrectos"));

    if (!passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
      throw new AuthenticationException("Incorrect password");
    }

    String accessToken = tokenGenerator.generateAccessToken(user.getId(), user.getEmail());
    String refreshToken = tokenGenerator.generateRefreshToken(user.getId());

    return new AuthenticationResult(accessToken, refreshToken);
  }

  @Override
  @Transactional
  public void logout(UUID userId) {
    // TODO: revocar todos los refresh tokens
  }
}
