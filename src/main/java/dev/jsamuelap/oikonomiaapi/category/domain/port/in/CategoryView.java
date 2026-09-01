package dev.jsamuelap.oikonomiaapi.category.domain.port.in;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public record CategoryView(UUID id, String name, FlowType flowType) {
}
