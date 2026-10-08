package dev.jsamuelap.oikonomiaapi.user.domain.port.in;

import java.util.UUID;

public record UserProfile(UUID id, String email, String firstName, String lastName) {
}
