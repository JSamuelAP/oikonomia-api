package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category summary response")
public record CategoryResponse(
  @Schema(description = "Category unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
  @Schema(description = "Category name", example = "Groceries") String name,
  @Schema(description = "Category flow type", example = "EXPENSE") FlowType flowType) {
}
