package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public record CreateCategoryCommand(UUID userId, String name, FlowType flowType) {
}
