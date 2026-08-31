package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

public record AuthenticateUserCommand(String email, String rawPassword) {
}
