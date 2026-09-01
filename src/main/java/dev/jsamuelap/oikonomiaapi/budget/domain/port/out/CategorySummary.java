package dev.jsamuelap.oikonomiaapi.budget.domain.port.out;

import java.util.UUID;

public record CategorySummary(UUID id, String name, String flowType) {
}
