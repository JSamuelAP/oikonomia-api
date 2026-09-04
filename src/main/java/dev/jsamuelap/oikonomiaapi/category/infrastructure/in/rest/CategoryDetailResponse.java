package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import java.time.Instant;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public record CategoryDetailResponse(UUID id, String name, FlowType flowType, Instant createdAt, Instant updatedAt) {
}
