package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.time.Instant;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public record CategoryDetail(UUID id, String name, FlowType flowType, Instant createdAt, Instant updatedAt) {
}
