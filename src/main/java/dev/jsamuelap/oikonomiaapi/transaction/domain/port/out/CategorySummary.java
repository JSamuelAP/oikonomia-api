package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.util.UUID;

public record CategorySummary(UUID id, String name, String flowType) {
}
