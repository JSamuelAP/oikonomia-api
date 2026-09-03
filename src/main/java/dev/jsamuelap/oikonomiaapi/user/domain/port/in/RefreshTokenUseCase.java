package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

public interface RefreshTokenUseCase {
  AuthenticationResult refresh(String rawRefreshToken);
}
