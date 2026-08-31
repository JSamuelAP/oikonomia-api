package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

public record AuthenticationResult(String accessToken, String refreshToken) {
}
