package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

public interface AuthenticateUserUseCase {
  AuthenticationResult authenticate(AuthenticateUserCommand command);
}
