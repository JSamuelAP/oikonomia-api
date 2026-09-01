package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.util.UUID;

public record CategorySummaryResponse(UUID id, String name, String flowType) {
}
