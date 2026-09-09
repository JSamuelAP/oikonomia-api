package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

public record CategoryView(UUID id, String name, String flowType, boolean deleted) {
}
