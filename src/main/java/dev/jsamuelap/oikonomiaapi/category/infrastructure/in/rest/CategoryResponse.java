package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public record CategoryResponse(UUID id, String name, FlowType flowType) {
}
