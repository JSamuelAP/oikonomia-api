package dev.jsamuelap.oikonomiaapi.shared.security.jwt;

import java.util.UUID;

public record AuthenticatedPrincipal(UUID userId, String email) {
}
