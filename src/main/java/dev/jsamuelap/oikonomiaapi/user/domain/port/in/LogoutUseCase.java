package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

import java.util.UUID;

public interface LogoutUseCase {
  void logout(UUID userId);
}
