package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

import java.util.UUID;

public interface GetUserUseCase {
  UserProfile get(UUID id);
}
