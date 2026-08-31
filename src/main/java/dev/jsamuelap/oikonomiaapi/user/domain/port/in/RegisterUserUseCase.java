package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

import java.util.UUID;

public interface RegisterUserUseCase {
  UUID registerUser(RegisterUserCommand command);
}
