package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

public record RegisterUserCommand(String firstName, String lastName, String email, String rawPassword) {
}
